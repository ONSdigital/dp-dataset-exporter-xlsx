package dp.api.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import dp.exceptions.FilterAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class DatasetAPIClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasetAPIClient.class);

    private static final String AUTH_HEADER_KEY = "Internal-Token";

    @Value("${DATASET_API_URL:http://localhost:22000}")
    private String datasetAPIURL;

    @Value("${DATASET_API_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Metadata getMetadata(URL url) throws MalformedURLException {
        LOGGER.info("getting dataset version data from the dataset api, url : {}", url);
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(AUTH_HEADER_KEY, token);
            HttpEntity entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<Metadata> responseEntity = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, Metadata.class);
            LOGGER.info("dataset api get response, url : {}, response {}", url, responseEntity.getStatusCode());
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException("get dataset data failed", e);
        }
    }

    public Metadata getMetadata(final String versionPath) throws MalformedURLException {
        return getMetadata(new URL(datasetAPIURL + versionPath + "/metadata"));
    }

    public void putVersionDownloads(final String datasetVersionURL, DownloadsList downloads) throws
            MalformedURLException {
        final String url = new URL(datasetAPIURL + datasetVersionURL).toString();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(AUTH_HEADER_KEY, token);

            HttpEntity<Version> entity = new HttpEntity<>(new Version(downloads), headers);
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RestClientException("incorrect status returned");
            }

        } catch (RestClientException e) {
            throw new FilterAPIException("put dataset version failed", e);
        }
    }
}
