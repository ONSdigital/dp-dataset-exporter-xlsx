package dp.configuration;

import java.net.URL;

import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

@Configuration
public class VaultConfiguration extends AbstractVaultConfiguration {

	@Override
	public VaultEndpoint vaultEndpoint() {
		VaultEndpoint vaultEndpoint = null;
		String addr = System.getenv("VAULT_ADDR");
		if (addr.length() == 0) {
			addr = "http://localhost:8200";
		}
		
		try {
			vaultEndpoint = VaultEndpoint.from(new URL(addr).toURI());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return vaultEndpoint;
	}

	@Override
	public ClientAuthentication clientAuthentication() {
		String token = System.getenv("VAULT_TOKEN");
		if (token.length() == 0) {
			token = "...";
		}
		return new TokenAuthentication(token);
	}

}
