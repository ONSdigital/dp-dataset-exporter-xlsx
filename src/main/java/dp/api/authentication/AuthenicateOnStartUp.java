package dp.api.authentication;

import dp.api.AuthUtils;
import org.apache.commons.lang3.StringUtils;
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

import static dp.logging.LogEvent.info;
import static dp.logging.LogEvent.error;

@Component
public class AuthenicateOnStartUp implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${ZEBEDEE_URL:}")
    private String zebedee_url;

    @Value("${SERVICE_AUTH_TOKEN:7049050e-5d55-440d-b461-319f8cdf6670}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        // If no url to zebedee is provided we assume the app is running on the webnet. As zebedee
        // will only be accessible from the publishing subnet
        if (StringUtils.isNotEmpty(zebedee_url) && !isAuthenticated()) {
            error().zebedeeURL(zebedee_url)
                    .log("failed to authenticate against zebedee. Please, try to authenticate again later");
        }
    }

    private boolean isAuthenticated() {
        try {
            final URL url = new URL(zebedee_url + "/identity");
            HttpEntity<ServiceIdentity> entity = AuthUtils.createAuthHeaders(token);
            ResponseEntity<ServiceIdentity> responseEntity = restTemplate.exchange(
                    url.toString(), HttpMethod.GET, entity, ServiceIdentity.class);
            info().zebedeeURL(zebedee_url).id(responseEntity.getBody().getId()).log("authenticated");
            return responseEntity.getStatusCodeValue() == HttpStatus.OK.value();
        } catch (RestClientException | IOException e) {
            error().logException(e, "failed to send http request to zebedee");
            return false;
        }
    }
}
