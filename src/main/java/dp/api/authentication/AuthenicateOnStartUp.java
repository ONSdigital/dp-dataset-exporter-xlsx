package dp.api.authentication;

import dp.api.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;

@Component
public class AuthenicateOnStartUp implements ApplicationListener<ApplicationReadyEvent> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenicateOnStartUp.class);

    @Value("${ZEBEDEE_URL:}")
    private String zebedee_url;

    @Value("${SERVICE_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        // If no url to zebedee is provided we assume the app is running on the webnet. As zebedee
        // will only be accessible from the publishing subnet
        if (StringUtils.isNotEmpty(zebedee_url) && !isAuthenticated()) {
            LOGGER.error("failed to authenticate against zebedee closing app");
            System.exit(1);
        }
    }

    private boolean isAuthenticated() {
        try {
            final URL url = new URL(zebedee_url + "/identity");
            HttpEntity<ServiceIdentity> entity = AuthUtils.createAuthHeaders(token);
            ResponseEntity<ServiceIdentity> responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, ServiceIdentity.class);
            LOGGER.info("authenicated as {}", responseEntity.getBody().getId());
            return responseEntity.getStatusCodeValue() == HttpStatus.OK.value();
        } catch (RestClientException | IOException e) {
            LOGGER.error("failed to sent http request to zebedee", e);
            return false;
        }
    }
}
