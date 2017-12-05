package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import dp.api.dataset.DatasetAPIClient;
import dp.api.dataset.Metadata;
import dp.api.filter.FilterAPIClient;
import dp.api.filter.Filter;
import dp.avro.ExportedFile;
import dp.xlsx.XLSXConverter;
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

/**
 * A class used to consume ExportedFile message from kafka
 */
@Component
public class Handler {

    private final static Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    @Value("${S3_BUCKET_NAME:csv-exported}")
    private String bucket;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private XLSXConverter converter;

    @Autowired
    private FilterAPIClient filterAPIClient;

    @Autowired
    private DatasetAPIClient datasetAPIClient;

    @KafkaListener(topics = "${KAFKA_TOPIC:common-output-created}")
    public void listen(final ExportedFile message) {

        LOGGER.info("exporting file to xlsx using filterID: {}", message.getFilterId());

        final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());

        try (final S3Object object = s3Client.getObject(uri.getBucket(), uri.getKey())) {

            final Filter filter = filterAPIClient.getFilter(message.getFilterId().toString());
            String datasetVersionURL = filter.getLinks().getVersion().getHref();
            final Metadata datasetMetadata = datasetAPIClient.getMetadata(datasetVersionURL);


            try (final Workbook workbook = converter.toXLSX(object.getObjectContent(), datasetMetadata);
                 final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {

                workbook.write(outputStream);

                byte[] xlsxBytes = outputStream.toByteArray();

                final long contentLength = xlsxBytes.length;
                final ObjectMetadata metadata = new ObjectMetadata();

                metadata.setContentLength(contentLength);
                final String key = message.getFilterId() + ".xlsx";

                try (final ByteArrayInputStream stream = new ByteArrayInputStream(xlsxBytes)) {

                    final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, stream, metadata);
                    s3Client.putObject(putObjectRequest);
                    final String downloadUri = s3Client.getUrl(bucket, key).toString();
                    filterAPIClient.addXLSXFile(message.getFilterId().toString(), downloadUri, contentLength);
                }
            }

        } catch (final IOException e) {
            LOGGER.error("error when exporting filter {}. exception : {}", message.getFilterId(), e);
        }

        LOGGER.info("exported completed for filterID: {}", message.getFilterId());
    }

}
