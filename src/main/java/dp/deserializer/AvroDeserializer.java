package dp.deserializer;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Arrays;
import java.util.Map;


/**
 * A class used by kafka to deserialize any avro messages to Java Objects
 *
 * @param <T> The type of object to be returned must extend SpecificRecordBase
 */
public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvroDeserializer.class);

    private final Class<T> targetType;

    public AvroDeserializer(final Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public void configure(final Map<String, ?> map, final boolean b) {

    }

    @Override
    public T deserialize(final String topic, final byte[] data) {
        try {
            T result = null;

            if (data != null) {
                LOGGER.debug("data='{}'", data);
                final DatumReader<T> datumReader = new SpecificDatumReader<>(targetType.newInstance().getSchema());
                final Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
                result = datumReader.read(null, decoder);
                LOGGER.debug("deserialized data='{}'", result);
            }
            return result;
        } catch (Exception ex) {
            throw new SerializationException(
                    "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", ex);
        }
    }

    @Override
    public void close() {

    }
}
