package dp.handler;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import dp.api.dataset.DatasetAPIClient;
import dp.api.dataset.MessageType;
import dp.api.dataset.WorkbookDetails;
import dp.api.dataset.models.Download;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.avro.ExportedFile;
import dp.exceptions.FilterAPIException;
import dp.xlsx.Converter;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static dp.api.dataset.MessageType.FILTER;
import static dp.api.dataset.MessageType.GetMessageType;
import static java.text.MessageFormat.format;

/**
 * A class used to consume ExportedFile message from kafka
 */
@Component
public class Handler {

    private final static Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    private static final String VERSION_DOWNLOADS_URL = "/datasets/{0}/editions/{1}/versions/{2}";

    @Value("${S3_BUCKET_NAME:csv-exported}")
    private String bucket;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private Converter converter;

    @Autowired
    private FilterAPIClient filterAPIClient;

    @Autowired
    private DatasetAPIClient datasetAPIClient;

    @KafkaListener(topics = "${KAFKA_TOPIC:common-output-created}")
    public void listen(final ExportedFile message) {
        MessageType messageType = GetMessageType(message);
        final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());

        try (final S3Object object = s3Client.getObject(uri.getBucket(), uri.getKey())) {
            if (FILTER.equals(messageType)) {
                handleFilterMessage(message, object);
            } else {
                handlePrePublishMessage(message, object);
            }
        } catch (final IOException | FilterAPIException e) {
            if (FILTER.equals(messageType)) {
                LOGGER.error("error when exporting filter {}. exception : {}", message.getFilterId(), e);
            } else {
                LOGGER.error("error when exporting pre publish message {}. exception : {}",
                        message.getFilename().toString(), e);
            }
            return;
        } catch (Exception e) {
            LOGGER.error("unexpected error throw while attempting to process message, {}", e.getMessage());
        }
        if (FILTER.equals(messageType)) {
            LOGGER.info("exported completed for filterID: {}", message.getFilterId());
        } else {
            LOGGER.info("exported completed for instance: datasetID: {}, edition: {}, version: {}",
                    message.getDatasetId(), message.getEdition(), message.getVersion());
        }
    }

    private void handleFilterMessage(ExportedFile message, S3Object object) throws IOException {
        LOGGER.info("handling filter message: ", message.getFilterId().toString());

        Filter filter = filterAPIClient.getFilter(message.getFilterId().toString());
        URL metadataURL = null;
        Metadata datasetMetadata = null;
        WorkbookDetails details = null;

        try {
            metadataURL = new URL(filter.getLinks().getVersion().getHref());
        } catch (MalformedURLException e) {
            throw new IOException(java.text.MessageFormat.format("error while attempting to create metadata URL, filterID {}, value: {}",
                    message.getFilterId().toString(), filter.getLinks().getVersion().getHref()), e);
        }

        try {
            datasetMetadata = datasetAPIClient.getMetadata(metadataURL);
        } catch (FilterAPIException e) {
            throw new IOException(java.text.MessageFormat.format("dataset api get metadata returned error. filterID {}, uri: {}",
                    message.getFilterId().toString(), metadataURL.toString()), e);
        }

        try {
            details = createWorkbook(object, datasetMetadata, message.getFilename().toString());
        } catch (IOException e) {
            throw new IOException(java.text.MessageFormat.format("error while attempting to create XLSX workbook, filterID: {}, filename: {}",
                    message.getFilterId().toString(), message.getFilename().toString()), e);
        }

        try {
            filterAPIClient.addXLSXFile(message.getFilterId().toString(), details.getDowloadURI(), details
                    .getContentLength());
        } catch (JsonProcessingException e) {
            throw new IOException(java.text.MessageFormat.format("filter api client addXLSXFile returned error, filterID: {}",
                    message.getFilterId().toString()), e);
        }
    }

    private void handlePrePublishMessage(ExportedFile message, S3Object object) throws IOException {
        LOGGER.info("handling pre-publish message: ", message.getFilename().toString());

        String versionURL = format(VERSION_DOWNLOADS_URL, message.getDatasetId(), message.getEdition(),
                message.getVersion());

        Metadata metadata = null;
        try {
            metadata = datasetAPIClient.getMetadata(versionURL);
        } catch (MalformedURLException | FilterAPIException e) {
            LOGGER.error("dataset api client error while attempting to get metadata, URL: {}", versionURL);
            throw e;
        }

        WorkbookDetails details = null;
        try {
            details = createWorkbook(object, metadata, message.getFilename().toString());

            try {
                DownloadsList downloadsList = new DownloadsList(new Download(details.getDowloadURI(),
                        String.valueOf(details.getContentLength())), null);

                datasetAPIClient.putVersionDownloads(versionURL, downloadsList);
            } catch (MalformedURLException |
                    FilterAPIException e)

            {
                LOGGER.error("dataset api PUT version returned error, filename: {}, URL: {}",
                        message.getFilename().toString(), versionURL);
                throw e;
            }


        } catch (IOException e) {
            LOGGER.error("error while attempting to create XLSX workbook, filename: {}",
                    message.getFilename().toString());
            throw e;
        }
        LOGGER.info("completed processing kafka message", message.getFilterId());
    }

    private WorkbookDetails createWorkbook(S3Object object, Metadata datasetMetadata, String filename) throws IOException {
        try (final Workbook workbook = converter.toXLSX(object.getObjectContent(), datasetMetadata);
             final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);

            byte[] xlsxBytes = outputStream.toByteArray();

            final long contentLength = xlsxBytes.length;
            final ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentLength(contentLength);
            final String key = filename + ".xlsx";

            try (final ByteArrayInputStream stream = new ByteArrayInputStream(xlsxBytes)) {
                final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, stream, metadata);
                s3Client.putObject(putObjectRequest);
                return new WorkbookDetails(s3Client.getUrl(bucket, key).toString(), contentLength);
            } catch (SdkClientException e) {
                LOGGER.error("error while attempting PUT XLSX workbook to S3 bucket, filename: {}, bucket: {}",
                        key, bucket);
                throw new FilterAPIException("error while attempting PUT XLSX workbook to S3 bucket", e);
            }
        } catch (IOException e) {
            LOGGER.error("error while attempting create XLSX workbook, filename: {}, bucket: {}", filename);
            throw e;
        }
    }
}
