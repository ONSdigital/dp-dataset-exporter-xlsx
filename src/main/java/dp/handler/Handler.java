package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
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
 * A class used to comsume ExportedFile message from kafka
 */
@Component
public class Handler {

    private final static Logger LOGGER = LoggerFactory.getLogger(Handler.class);

    @Value("${S3_BUCKET:csv-exported}")
    private String bucket;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private XLXSConverter converter;

    @KafkaListener(topics = "convert-v4-file")
    public void listen(final ExportedFile message)  {
        LOGGER.debug("exporting file to xlsx using filterID: %s", message.getFilterId());
        final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());
        try {
            try (final S3Object object = s3Client.getObject(uri.getBucket(), uri.getKey())) {
                try (final ByteArrayOutputStream xls = converter.toXLXS(object.getObjectContent())) {
                    final long contentLength = xls.toByteArray().length;
                    final ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(contentLength);
                    final String key = message.getFilterId() + ".xlsx";
                    s3Client.putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(xls.toByteArray()), metadata));
                }
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        LOGGER.debug("exported completed for filterID: %s", message.getFilterId());
    }

}
