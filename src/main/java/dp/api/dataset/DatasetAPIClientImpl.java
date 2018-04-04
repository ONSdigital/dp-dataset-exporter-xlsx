package dp.api.dataset;

import dp.api.AuthUtils;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.Version;
import dp.exceptions.FilterAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

import static java.text.MessageFormat.format;

@Component
public class DatasetAPIClientImpl implements DatasetAPIClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasetAPIClientImpl.class);

    @Value("${DATASET_API_URL:http://localhost:22000}")
    private String datasetAPIURL;

    @Deprecated
    @Value("${DATASET_API_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Value("${SERVICE_AUTH_TOKEN:7049050e-5d55-440d-b461-319f8cdf6670}")
    private String serviceToken;

    @Autowired
    private RestTemplate restTemplate;

    public Metadata getMetadata(final String versionPath) throws MalformedURLException, FilterAPIException {
        URL metadataURL = new URL(datasetAPIURL + versionPath + "/metadata");

        LOGGER.info("getting dataset version data from the dataset api, url : {}", metadataURL);
        try {
            HttpEntity entity = AuthUtils.createHeaders(serviceToken, token, null);
            ResponseEntity<Metadata> responseEntity = restTemplate.exchange(metadataURL.toString(), HttpMethod.GET, entity, Metadata.class);
            LOGGER.info("dataset api get response, url : {}, response {}", metadataURL.toString(),
                    responseEntity.getStatusCode());
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException(format("get dataset metadata returned an error, URL {0}", metadataURL.toString()), e);
        }

    }

    public void putVersionDownloads(final String datasetVersionURL, DownloadsList downloads) throws
            MalformedURLException, FilterAPIException {
        final String url = new URL(datasetAPIURL + datasetVersionURL).toString();

        try {
            HttpEntity<Version> entity = AuthUtils.createHeaders(serviceToken, token, new Version(downloads));
            ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RestClientException("incorrect status returned");
            }
        } catch (RestClientException e) {
            throw new FilterAPIException("put dataset version failed", e);
        }
    }
}
