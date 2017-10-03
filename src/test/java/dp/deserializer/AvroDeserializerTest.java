package dp.deserializer;

import dp.avro.ExportedFile;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvroDeserializerTest {

    private AvroDeserializer<ExportedFile> deserializer = new AvroDeserializer<>(ExportedFile.class);

    @Test(expected = org.apache.kafka.common.errors.SerializationException.class)
    public void invalidAvroMessage() {
        deserializer.deserialize("test", new byte[] {0x05, 0x05, 0x02, 0x02, 0x02, 0x07});
        //  deserializer.deserialize("test", new byte[] {0x06, 0x05, 0x02, 0x02, 0x02, 0x07});
    }

    @Test
    public void validAvroMessage() {
        final ExportedFile exportedFile = deserializer.deserialize("test", new byte[] {0x06, 0x45, 0x49, 0x42, 0x02, 0x47});
        assertThat(exportedFile.getFilterId().toString()).isEqualTo("EIB");
        assertThat(exportedFile.getS3URL().toString()).isEqualTo("G");
    }
}
