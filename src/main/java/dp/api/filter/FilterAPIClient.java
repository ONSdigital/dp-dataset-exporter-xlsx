package dp.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dp.api.AuthUtils;
import dp.exceptions.FilterAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FilterAPIClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(FilterAPIClient.class);

    @Value("${FILTER_API_URL:http://localhost:22100}")
    private String filterAPIURL;

    @Value("${SERVICE_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void addXLSXFile(final String id, final String s3Location, final long size) throws JsonProcessingException {

        final String url = UriComponentsBuilder.fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}").buildAndExpand(id).toUriString();
        final String sizeToString = Long.toString(size);

        final PutFileRequest r = new PutFileRequest(new Downloads(new XLSXFile(s3Location, sizeToString)));

        try {

            LOGGER.info("updating filter api, url : {}, json : {}", url, objectMapper.writeValueAsString(r));
            restTemplate.put(url, AuthUtils.createHeaders(token, r));

        } catch (RestClientException e) {
            throw new FilterAPIException("expected 200 status code", e);
        }
    }


    public Filter getFilter(final String filterID) {

        final String url = UriComponentsBuilder
                .fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}")
                .buildAndExpand(filterID).toUriString();

        LOGGER.info("getting filter data from the filter api, url : {}", url);

        try {

            ResponseEntity<Filter> responseEntity = restTemplate.getForEntity(url, Filter.class);
            LOGGER.info("filter api get response, url : {}, response {}", url, responseEntity.getStatusCode());
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException("get filter data failed", e);
        }
    }
}
