package dp.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.vault.core.VaultTemplate;

import dp.handler.Handler;
import dp.xlsx.Converter;

@Configuration
public class TestConfig {
	
	@Bean
	@Primary
	VaultTemplate getTemplate() {
		return Mockito.mock(VaultTemplate.class);
	}
	
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
