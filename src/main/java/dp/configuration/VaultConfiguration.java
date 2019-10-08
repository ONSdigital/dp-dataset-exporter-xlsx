package dp.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Configuration
public class VaultConfiguration extends AbstractVaultConfiguration {

    @Override
    public VaultEndpoint vaultEndpoint() {
        VaultEndpoint vaultEndpoint = null;
        String addr = System.getenv("VAULT_ADDR");
        if (StringUtils.isEmpty(addr)) {
            addr = "http://localhost:8200";
        }

        try {
            vaultEndpoint = VaultEndpoint.from(new URL(addr).toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return vaultEndpoint;
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        String token = System.getenv("VAULT_TOKEN");
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("expected VAULT_TOKEN config but none found");
        }
        return new TokenAuthentication(token);
    }

}
