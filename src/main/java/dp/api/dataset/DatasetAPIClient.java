package dp.api.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import dp.exceptions.FilterAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class DatasetAPIClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasetAPIClient.class);

    @Value("${DATASET_API_URL:http://localhost:22000}")
    private String datasetAPIURL;

    @Value("${DATASET_API_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public Metadata getMetadata(final String datasetVersionURL) throws MalformedURLException {

        final String versionPath = new URL(datasetVersionURL).getPath();
        final String url = new URL(datasetAPIURL + versionPath + "/metadata").toString();

        LOGGER.info("getting dataset version data from the dataset api, url : {}", url);

        try {

            ResponseEntity<Metadata> responseEntity = restTemplate.getForEntity(url, Metadata.class);
            LOGGER.info("dataset api get response, url : {}, response {}", url, responseEntity.getStatusCode());
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException("get dataset data failed", e);
        }
    }
}
