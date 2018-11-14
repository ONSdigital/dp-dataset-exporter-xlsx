package dp.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dp.api.AuthUtils;
import dp.api.authentication.ServiceIdentity;
import dp.exceptions.FilterAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    @Deprecated
    @Value("${FILTER_API_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    @Value("${SERVICE_AUTH_TOKEN:7049050e-5d55-440d-b461-319f8cdf6670}")
    private String serviceToken;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void addXLSXFile(final String id, final String s3Location, final long size, boolean filterIsPublished) throws JsonProcessingException {

        final String url = UriComponentsBuilder.fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}").buildAndExpand(id).toUriString();
        final String sizeToString = Long.toString(size);

        XLSXFile xlsxFile = new XLSXFile();
        xlsxFile.setSize(sizeToString);

        if (filterIsPublished) {
            xlsxFile.setPublicUrl(s3Location);
        } else {
            xlsxFile.setPrivateUrl(s3Location);
        }

        final PutFileRequest r = new PutFileRequest(new Downloads(xlsxFile));

        try {

            LOGGER.info("updating filter api, url : {}, json : {}", url, objectMapper.writeValueAsString(r));
            restTemplate.put(url, AuthUtils.createHeaders(serviceToken, token, r));

        } catch (RestClientException e) {
            throw new FilterAPIException("expected 200 status code", e);
        }
    }

    public void setToComplete(final String id) throws JsonProcessingException {
      final String url = UriComponentsBuilder.fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}").buildAndExpand(id).toUriString();

      XLSXFile xlsxFile = new XLSXFile();
      xlsxFile.setSkipped(true);

      final PutFileRequest r = new PutFileRequest(new Downloads(xlsxFile));

      try {
          LOGGER.info("updating filter api, url : {}, json : {}", url, objectMapper.writeValueAsString(r));
          restTemplate.put(url, AuthUtils.createHeaders(serviceToken, token, r));

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

            HttpEntity<ServiceIdentity> entity = AuthUtils.createAuthHeaders(serviceToken);
            ResponseEntity<Filter> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Filter.class);

            LOGGER.info("filter api get response, url : {}, response {}", url, responseEntity.getStatusCode());
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException("get filter data failed", e);
        }
    }
}
