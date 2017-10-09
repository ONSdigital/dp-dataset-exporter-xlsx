package dp.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FilterAPIClient {

    @Value("${FILTER_API_URL:http://localhost:22100}")
    private String filterAPIURL;

    @Value("${FILTER_API_AUTH_TOKEN:FD0108EA-825D-411C-9B1D-41EF7727F465}")
    private String token;

    private RestTemplate restTemplate = new RestTemplate();

    public void addXLSXFile(final String id, final String s3Location, final long size) {
        final String url = UriComponentsBuilder.fromHttpUrl(filterAPIURL + "/filters/{filterId}").buildAndExpand(id).toUriString();
        final HttpHeaders headers = new HttpHeaders();
        final String sizeToString = Long.toString(size);
        headers.add("internal-token", token);
        Request r = new Request(new Downloads(new XlSFile(s3Location, sizeToString)));
        restTemplate.put(url, new HttpEntity<>(r, headers));
    }

}
