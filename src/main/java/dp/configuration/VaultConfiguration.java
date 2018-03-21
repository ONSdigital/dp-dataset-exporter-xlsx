package dp.configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.context.annotation.Configuration;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.annotation.VaultPropertySource.Renewal;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

@Configuration
@VaultPropertySource(value="secret/shared/psk", renewal = Renewal.ROTATE)
public class VaultConfiguration extends AbstractVaultConfiguration {

	@Override
	public VaultEndpoint vaultEndpoint() {
		VaultEndpoint vaultEndpoint = null;
		try {
			vaultEndpoint = VaultEndpoint.from(new URL(System.getenv("VAULT_ADDR")).toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vaultEndpoint;
	}

	@Override
	public ClientAuthentication clientAuthentication() {
		return new TokenAuthentication(System.getenv("VAULT_TOKEN"));
	}

}
