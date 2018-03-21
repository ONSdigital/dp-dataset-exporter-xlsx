package dp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AppConfiguration {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
