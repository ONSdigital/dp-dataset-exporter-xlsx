package dp.configuration;

import dp.avro.ExportedFile;
import dp.deserializer.AvroDeserializer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

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

    // PEM key
    @Value("${KAFKA_SEC_CLIENT_KEY:}")
    private String kafkaSecClientKey;

    // PEM cert
    @Value("${KAFKA_SEC_CLIENT_CERT:}")
    private String kafkaSecClientCert;

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
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            if (!kafkaSecClientKey.isEmpty()) {
                props.put(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, kafkaSecClientKey);
                props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
                props.put(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, kafkaSecClientCert);
            }
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
