package dp.deserializer;

import dp.avro.ExportedFile;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class AvroDeserializerTest {

    private AvroDeserializer<ExportedFile> deserializer = new AvroDeserializer<>(ExportedFile.class);
    private String filterID = "666";
    private String datasetID = "123";
    private String instanceID = "456";
    private String edition = "2017";
    private String version = "1";
    private String filename = "morty";
    private String s3URL = "s3://bucket/v4.csv";

    @Test(expected = org.apache.kafka.common.errors.SerializationException.class)
    public void invalidAvroMessage() {
        deserializer.deserialize("test", new byte[]{0x05, 0x05, 0x02, 0x02, 0x02, 0x07});
    }

    @Test
    public void validAvroMessage() throws Exception {
        final ExportedFile exportedFile = deserializer.deserialize("test", getValidMessageBytes());
        assertThat(exportedFile.getFilterId().toString()).isEqualTo(filterID);
        assertThat(exportedFile.getS3URL().toString()).isEqualTo(s3URL);
        assertThat(exportedFile.getInstanceId().toString()).isEqualTo(instanceID);
        assertThat(exportedFile.getDatasetId().toString()).isEqualTo(datasetID);
        assertThat(exportedFile.getEdition().toString()).isEqualTo(edition);
        assertThat(exportedFile.getVersion().toString()).isEqualTo(version);
        assertThat(exportedFile.getFilename().toString()).isEqualTo(filename);
    }

    private byte[] getValidMessageBytes() throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            DatumWriter<ExportedFile> writer = new SpecificDatumWriter<ExportedFile>(ExportedFile.getClassSchema());
            ExportedFile f = new ExportedFile(filterID, s3URL, instanceID, datasetID, edition, version, filename);
            writer.write(f, encoder);
            encoder.flush();
            return out.toByteArray();
        }
    }
}
