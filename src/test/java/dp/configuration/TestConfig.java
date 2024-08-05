package dp.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import dp.handler.Handler;
import dp.xlsx.Converter;

@Configuration
public class TestConfig {
	
	@Bean
	@Primary
	Handler getHandler() {
		return new Handler();
	}
	
	@Bean
	@Primary
	Converter getConverter() {
		return new Converter();
	}
}
