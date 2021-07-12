package dp.configuration;

import dp.avro.ExportedFile;
import dp.deserializer.AvroDeserializer;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.util.Base64.decode;

/**
 * A spring configuration for kafka consumer
 */
@Configuration
public class KafkaConfiguration {

    @Value("${KAFKA_ADDR:localhost:9092}")
    private String kafkaAddress;

    @Value("${KAFKA_SEC_PROTO:}")
    private String kafkaSecProtocol;

    // filepath for key (not used if KAFKA_SEC_CLIENT_KEY_P12 set)
    @Value("${KAFKA_SEC_CLIENT_KEY:}")
    private String kafkaSecClientKeyFile;

    // base64-encoded key in PKCS12 format (if blank, use KAFKA_SEC_CLIENT_KEY)
    @Value("${KAFKA_SEC_CLIENT_KEY_P12:}")
    private String kafkaSecClientKey;

    @Value("${KAFKA_GROUP:dp-dataset-exporter-xlsx}")
    private String kafkaGroup;
    
    private static final int POLL_TIMEOUT = 30000;
    private static final String KEY_FILE_PREFIX = "client-key";

    /**
     * We override the default consumer factory to place a AvroDeserializer within
     * the DefaultKafkaConsumerFactory
     *
     * @return A DefaultKafkaConsumerFactory with a AvroDeserializer
     */
    @Bean
    ConsumerFactory<String, AvroDeserializer<ExportedFile>> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroup);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, POLL_TIMEOUT);

        if (kafkaSecProtocol.equals("TLS")) {
            props.put("security.protocol", "SSL");
            if (!kafkaSecClientKey.isEmpty()) {
                byte[] kafkaSecClientKeyBytes  = decode(kafkaSecClientKey);
                // Kafka versions before 2.7 needs the above to be in files
                File keyFile;
                try {
                    keyFile  = new File(Files.createTempFile(KEY_FILE_PREFIX, ".p12").toString());
                    FileUtils.writeByteArrayToFile(keyFile, kafkaSecClientKeyBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                props.put("ssl.keystore.location",   keyFile.toString());

            } else {
                // key already in file
                props.put("ssl.keystore.location", kafkaSecClientKeyFile);
            }
            props.put("ssl.keystore.password", "");
            props.put("ssl.keystore.type", "PKCS12");
        }

        AvroDeserializer<ExportedFile> deserializer = new AvroDeserializer<>(ExportedFile.class);
        return new DefaultKafkaConsumerFactory(props, new StringDeserializer(), deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, AvroDeserializer<ExportedFile>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AvroDeserializer<ExportedFile>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
