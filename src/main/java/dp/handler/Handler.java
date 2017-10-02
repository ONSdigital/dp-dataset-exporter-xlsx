package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import dp.avro.ExportedFile;
import dp.dataset.Dataset;
import dp.dataset.DatasetAPI;
import dp.xlsx.XLXSConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class Handler {

    @Value("${S3_BUCKET:csv-exported}")
    private String bucket;

    @Autowired
    private DatasetAPI datasetAPI;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private XLXSConverter converter;

    @KafkaListener(topics = "${KAFKA_GROUP:convert-v4-file}")
    public void listen(final ExportedFile message) throws IOException {
        final AmazonS3URI uri = new AmazonS3URI(message.getS3URL().toString());
        try (final S3Object object = s3Client.getObject(uri.getBucket(), uri.getKey())) {
            Dataset dataset = datasetAPI.getDataset("");
            try (final ByteArrayOutputStream xls = converter.toXLXS(dataset, object.getObjectContent())) {
                final long contentLength =  xls.toByteArray().length;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(contentLength);
                final String key = message.getFilterId() + ".xlsx";
                final PutObjectResult result = s3Client.putObject( new PutObjectRequest(bucket, key, new ByteArrayInputStream(xls.toByteArray()), metadata));
                Files.write(Paths.get("test.xlsx"), xls.toByteArray());
            }
        }
    }

}
