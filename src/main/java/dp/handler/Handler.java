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
import dp.api.dataset.models.Version;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.avro.ExportedFile;
import dp.exceptions.FilterAPIException;
import dp.s3crypto.S3Crypto;
import dp.xlsx.Converter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultOperations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import static dp.api.dataset.MessageType.FILTER;
import static dp.api.dataset.MessageType.GetMessageType;
import static java.text.MessageFormat.format;
import static dp.logging.LogEvent.info;
import static dp.logging.LogEvent.error;


/**
 * A class used to consume ExportedFile message from kafka
 */
@Component
public class Handler {

    private static final String VERSION_DOWNLOADS_URL = "/datasets/{0}/editions/{1}/versions/{2}";
    private static final String INSTANCE_URL = "/instances/{0}";
    private static final String PUBLISHED_STATE = "published";
    
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss'Z'");
    private static final TimeZone utc = TimeZone.getTimeZone("UTC");

    @Value("${S3_BUCKET_URL:}")
    private String bucketUrl;

    @Value("${S3_BUCKET_S3_URL:}")
    private String bucketS3Url;

    @Value("${S3_BUCKET_NAME:csv-exported}")
    private String bucket;

    @Value("${S3_PRIVATE_BUCKET_NAME:csv-exported}")
    private String privateBucket;

    @Value("${VAULT_PATH:secret/shared/psk}")
    private String vaultPath;

    @Value("${DOWNLOAD_SERVICE_URL:http://localhost:23600}")
    private String downloadServiceUrl;

    @Value("${FULL_DATASET_FILE_PREFIX:full-datasets/}")
    private String fullDatasetFilePrefix;

    @Value("${FILTERED_DATASET_FILE_PREFIX:filtered-datasets/}")
    private String filteredDatasetFilePrefix;

    // 1M row limit in excel - need some buffer for headers/footnotes in file
    @Value("${MAX_OBSERVATION_COUNT:999900}")
    private Integer maxObservationCount;

    @Autowired
    @Qualifier("s3-client")
    private AmazonS3 s3Client;

    @Autowired
    @Qualifier("crypto-client")
    private S3Crypto s3Crypto;

    @Autowired
    private VaultOperations vaultOperations;

    @Autowired
    private Converter converter;

    @Autowired
    private FilterAPIClient filterAPIClient;

    @Autowired
    private DatasetAPIClient datasetAPIClient;

    @KafkaListener(topics = "${KAFKA_TOPIC:common-output-created}")
    public void listen(final ExportedFile message, Acknowledgment ack) {
        // reset traceID every time that we receive a new Kafka Message
        // This should be part of dp-logging, which should handle Kafka Messages in a more elegant way,
        // similarly to how it handles HTTP requests at the moment.
        info().traceID(null);

        MessageType messageType = GetMessageType(message);

        try {

            Integer rowCount = message.getRowCount() != null ? message.getRowCount() : 0;
            String logMessage = "attempting to export";

            if (message.getDatasetId() != null) {
                info().datasetID(message.getDatasetId().toString()).rowCount(rowCount.toString()).log(logMessage);
            } else if (message.getInstanceId() != null) {
                info().instanceID(message.getInstanceId().toString()).rowCount(rowCount.toString()).log(logMessage);
            } else if (message.getFilterId() != null){
                info().filterID(message.getFilterId().toString()).rowCount(rowCount.toString()).log(logMessage);
            }

            if (FILTER.equals(messageType)) {
                if (rowCount > maxObservationCount) {
                  completeFilter(message);
                } else {
                  handleFilterMessage(message);
                }
            } else {
              if (rowCount <= maxObservationCount) {
                  handleFullDownloadMessage(message);
              } else {
                  info().instanceID(message.getInstanceId().toString()).rowCount(rowCount.toString())
                          .log("full download too large to export");
              }
            }
        } catch (final IOException | FilterAPIException e) {
            if (FILTER.equals(messageType)) {
                error().filterID(message.getFilterId().toString()).logException(e, "error when exporting filter");
            } else {
                error().fileName(message.getFilename().toString()).
                        logException(e, "error when exporting pre publish message");
            }
            return;
        } catch (Exception e) {
            error().logException(e, "unexpected error throw while attempting to process message");
        } finally {
            ack.acknowledge(); // always commit the offset, regardless of any error
        }

        // log 'export completed' message
        if (FILTER.equals(messageType)) {
            info().filterID(message.getFilterId().toString()).log("exported completed");
        } else {
            info().datasetID(message.getDatasetId().toString()).edition(message.getEdition().toString())
                    .version(message.getVersion().toString()).log("exported completed");
        }
    }

    private String getVersionState(ExportedFile message) throws MalformedURLException, FilterAPIException {

        String path;
        if (message.getInstanceId().length() == 0) {
            path = format(VERSION_DOWNLOADS_URL, message.getDatasetId(), message.getEdition(), message.getVersion());
        } else {
            path = format(INSTANCE_URL, message.getInstanceId());
        }

        Version version = datasetAPIClient.getVersion(path);

        return version.getState();
    }

    private void completeFilter(ExportedFile message) throws IOException, DecoderException {
        info().filterID(message.getFilterId().toString()).rowCount(message.getRowCount().toString())
                .log("filter too large to export");

      try {
          filterAPIClient.setToComplete(message.getFilterId().toString());
      } catch (JsonProcessingException e) {
          throw new IOException(format("filter api client setToComplete returned error, filterID: {0}",
                  message.getFilterId().toString()), e);
      }
    }

    private void handleFilterMessage(ExportedFile message) throws IOException, DecoderException {
        final String filterId = message.getFilterId().toString();
        final String datasetId = message.getDatasetId().toString();
        final String edition = message.getEdition().toString();
        final String version = message.getVersion().toString();

        info().filterID(filterId).log("handling filter");

        String s3uri = getS3URL(message.getS3URL().toString());
        Filter filter = filterAPIClient.getFilter(filterId);

        final AmazonS3URI uri = new AmazonS3URI(s3uri);
        final S3Object object = getObject(uri.getBucket(), uri.getKey(), filter.isPublished());
        info().filterID(filterId).log("successfully got s3 object");

        String metadataURL;
        try {
            URL url = new URL(filter.getLinks().getVersion().getHref());
            metadataURL = url.getPath();
        } catch (MalformedURLException e) {
            throw new IOException(format("error while attempting to create metadata URL filterID {0}, value: {1}",
                    filterId, filter.getLinks().getVersion().getHref()), e);
        }

        Metadata datasetMetadata;
        try {
            datasetMetadata = datasetAPIClient.getMetadata(metadataURL);
        } catch (FilterAPIException e) {
            throw new IOException(format("dataset api get metadata returned error. filterID {0}, uri: {1}",
                    filterId, metadataURL.toString()), e);
        }
        
        df.setTimeZone(utc);
        String datetime = df.format(new Date());

        final String filename = filteredDatasetFilePrefix + filterId + "/" + datasetId + "-" + edition + "-v" + version + "-filtered-" + datetime + ".xlsx";
        WorkbookDetails details;
        try {
            details = createWorkbook(object, datasetMetadata, filename, filter.isPublished());
        } catch (IOException e) {
            throw new IOException(format("error while attempting to create xlsx workbook filterID: {0}, filename: {1}",
                    filterId, message.getFilename().toString()), e);
        }

        try {
            String publicUrl = getDownloadUrl(filter.isPublished(), filename, details);
            filterAPIClient.addXLSXFile(filterId,
                    details.getDownloadURI(), publicUrl,
                    details.getContentLength(), filter.isPublished());
        } catch (JsonProcessingException e) {
            throw new IOException(format("filter api client addXLSXFile returned error, filterID: {0}",
                    filterId), e);
        }
    }

    private void handleFullDownloadMessage(ExportedFile message)
            throws IOException, DecoderException {

        info().fileName(message.getFilename().toString()).log("handling pre-publish");

        String versionURL = format(VERSION_DOWNLOADS_URL, message.getDatasetId(), message.getEdition(),
                message.getVersion());
        String state = getVersionState(message);
        boolean isPublished = PUBLISHED_STATE.equals(state);
        String s3uri = getS3URL(message.getS3URL().toString());

        final AmazonS3URI uri = new AmazonS3URI(s3uri);
        S3Object object = getObject(uri.getBucket(), uri.getKey(), PUBLISHED_STATE.equals(state));
        info().versionURL(versionURL).log("successfully got s3Object");

        Metadata metadata;
        try {
            metadata = datasetAPIClient.getMetadata(versionURL);
        } catch (MalformedURLException | FilterAPIException e) {
            error().versionURL(versionURL).logException(e, "dataset api client error while attempting to get metadata");
            throw e;
        }

        WorkbookDetails details;
        try {


            final String filename = fullDatasetFilePrefix + message.getFilename().toString() + ".xlsx";

            details = createWorkbook(object, metadata, filename, isPublished);

            try {
                String downloadUrl = downloadServiceUrl + "/downloads" + format(VERSION_DOWNLOADS_URL, message.getDatasetId(),
                        message.getEdition(), message.getVersion()) + ".xlsx";

                Download download = new Download(downloadUrl, String.valueOf(details.getContentLength()));
                String downloadableUrl = getDownloadUrl(isPublished, filename, details);
                if (isPublished) {
                    download.setPublicState(downloadableUrl);
                } else {
                    download.setPrivateState(downloadableUrl);
                }
                DownloadsList downloadsList = new DownloadsList(download, null);

                datasetAPIClient.putVersionDownloads(versionURL, downloadsList);

            } catch (MalformedURLException | FilterAPIException e) {
                error().fileName(message.getFilename().toString()).versionURL(versionURL)
                        .logException(e, "dataset api put version returned error");
                throw e;
            }

        } catch (IOException e) {
            error().fileName(message.getFilename().toString())
                    .logException(e, "error while attempting to create xlsx workbook");
            throw e;
        }
        info().filterID(message.getFilterId().toString()).log("completed processing kafka message");
    }

    private WorkbookDetails createWorkbook(S3Object object, Metadata datasetMetadata, String filename,
                                           boolean isPublished) throws IOException {
        try (final Workbook workbook = converter.toXLSX(object.getObjectContent(), datasetMetadata);
             final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);

            byte[] xlsxBytes = outputStream.toByteArray();

            final long contentLength = xlsxBytes.length;
            final ObjectMetadata metadata = new ObjectMetadata();

            metadata.setContentLength(contentLength);

            try (final ByteArrayInputStream stream = new ByteArrayInputStream(xlsxBytes)) {
                final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, filename, stream, metadata);
                if (isPublished) {
                    s3Client.putObject(putObjectRequest);
                } else {
                    putObjectRequest.setBucketName(privateBucket);
                    s3Crypto.putObject(putObjectRequest);
                }
                return new WorkbookDetails(s3Client.getUrl(bucket, filename).toString(), contentLength);
            } catch (SdkClientException e) {
                error().fileName(filename).bucket(bucket)
                        .logException(e, "error while attempting put xlsx workbook to s3 bucket");
                throw new FilterAPIException("error while attempting PUT XLSX workbook to S3 bucket", e);
            }
        } catch (IOException e) {
            error().fileName(filename).bucket(bucket).logException(e, "error while attempting to create xlsx workbook");
            throw e;
        }
    }

    private S3Object getObject(String bucket, String key, boolean isPublished) throws SdkClientException, DecoderException {
        if (isPublished) {
            return s3Client.getObject(bucket, key);
        }
        return s3Crypto.getObject(bucket, key);
    }

    private String getDownloadUrl(boolean isPublished, String filePath, WorkbookDetails details) {
        if (isPublished && StringUtils.isNotEmpty(bucketUrl)) {
            return bucketUrl + "/" + filePath;
        }
        return details.getDownloadURI();
    }

    /** 
     * getS3URL returns an S3-compatible version of url:
     * replaces the CloudFront/S3 bucket URL (bucketUrl) prefix in url
     * with the more S3-lib-friendly prefix (bucketS3Url).
     * Both vars must be non-empty for the replacement to occur.
     */
    String getS3URL(String url) {
        if (StringUtils.isNotEmpty(bucketUrl) && StringUtils.isNotEmpty(bucketS3Url)) {
            url = url.replace(bucketUrl, bucketS3Url);
        }
        return url;
    }

    public void setBucketS3Url(String newBucketS3Url) {
        bucketS3Url = newBucketS3Url;
    }

    public void setBucketUrl(String newBucketUrl) {
        bucketUrl = newBucketUrl;
    }
}
