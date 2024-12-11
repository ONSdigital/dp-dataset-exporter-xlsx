package dp.api.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dp.api.AuthUtils;
import dp.api.authentication.ServiceIdentity;
import dp.exceptions.FilterAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static dp.logging.LogEvent.info;

@Component
public class FilterAPIClient {

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

    public void addXLSXFile(final String id, final String s3Location, final String s3PublicUrl, final long size, boolean filterIsPublished) throws JsonProcessingException {

        final String url = UriComponentsBuilder.fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}").buildAndExpand(id).toUriString();
        final String sizeToString = Long.toString(size);

        XLSXFile xlsxFile = new XLSXFile();
        xlsxFile.setSize(sizeToString);

        if (filterIsPublished) {
            xlsxFile.setPublicUrl(s3PublicUrl);
        } else {
            xlsxFile.setPrivateUrl(s3Location);
        }

        final PutFileRequest r = new PutFileRequest(new Downloads(xlsxFile));

        try {
            info().url(url).json(objectMapper.writeValueAsString(r)).log("updating filter api");
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
          info().url(url).json(objectMapper.writeValueAsString(r)).log("updating filter api");
          restTemplate.put(url, AuthUtils.createHeaders(serviceToken, token, r));

      } catch (RestClientException e) {
          throw new FilterAPIException("expected 200 status code", e);
      }
    }

    public Filter getFilter(final String filterID) {

        final String url = UriComponentsBuilder
                .fromHttpUrl(filterAPIURL + "/filter-outputs/{filterId}")
                .buildAndExpand(filterID).toUriString();
        info().url(url).log("getting filter data from the filter api");

        try {

            HttpEntity<ServiceIdentity> entity = AuthUtils.createAuthHeaders(serviceToken);
            ResponseEntity<Filter> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Filter.class);

            info().url(url).statusCode(HttpStatus.valueOf(responseEntity.getStatusCode().value())).log("filter api get response");
            return responseEntity.getBody();

        } catch (RestClientException e) {
            throw new FilterAPIException("get filter data failed", e);
        }
    }
}
