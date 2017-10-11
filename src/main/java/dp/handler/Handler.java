package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import dp.api.FilterAPIClient;
import dp.avro.ExportedFile;
import dp.xlsx.XLXSConverter;
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
    private XLXSConverter converter;

    @Autowired
    private FilterAPIClient filterAPIClient;

    @KafkaListener(topics = "${KAFKA_TOPIC:common-output-created}")
    public void listen(final ExportedFile message) {
        LOGGER.info("exporting file to xlsx using filterID: {}", message.getFilterId());
        final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());
        try {
            try (final S3Object object = s3Client.getObject(uri.getBucket(), uri.getKey())) {
                try (final ByteArrayOutputStream xls = converter.toXLXS(object.getObjectContent())) {
                    final long contentLength = xls.toByteArray().length;
                    final ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(contentLength);
                    final String key = message.getFilterId() + ".xlsx";
                    try (final ByteArrayInputStream stream = new ByteArrayInputStream(xls.toByteArray())) {
                        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, stream, metadata);
                        s3Client.putObject(putObjectRequest);
                        final String downloadUri = s3Client.getUrl(bucket, key).toString();
                        filterAPIClient.addXLSXFile(message.getFilterId().toString(), downloadUri, contentLength);
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.error("error when exporting filter {}. exception : {}", message.getFilterId(), e);
        }
        LOGGER.info("exported completed for filterID: {}", message.getFilterId());
    }

}
