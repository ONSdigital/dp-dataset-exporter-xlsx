package dp.api.dataset;

import dp.api.dataset.models.Download;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.Version;
import dp.exceptions.FilterAPIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Created by dave on 12/12/2017.
 */
public class DatasetAPIClientImplTest {

    private static final String METADATA_URI = "/datasets/123/editions/2017/version/1/metadata";

    private static final String VERSION_URI = "/datasets/123/editions/2017/version/1";

    private static final String DATASET_API_URL = "http://localhost:22000";

    private static final String METADATA_URL = DATASET_API_URL + VERSION_URI + "/metadata";

    private static final String VERSION_URL = DATASET_API_URL + VERSION_URI;

    private static final String AUTH_TOKEN = "666";
    private static final String EXPECTED_AUTH_HEADER = "Bearer " + AUTH_TOKEN;
    private static final String AUTH_TOKEN_KEY_OLD = "Internal-Token";

    private static final String AUTH_TOKEN_KEY = "Authorization";

    private DatasetAPIClientImpl api;

    @Mock
    private RestTemplate restTemplateMock;

    @Mock
    private ResponseEntity<Metadata> metadataResponseEntity;

    @Mock
    private ResponseEntity
            versionResponseEntity;

    private Metadata metadata;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        api = new DatasetAPIClientImpl();

        this.metadata = new Metadata();

        setField(api, "restTemplate", restTemplateMock);
        setField(api, "datasetAPIURL", DATASET_API_URL);
        setField(api, "token", AUTH_TOKEN);
        setField(api, "serviceToken", AUTH_TOKEN);
    }

    @Test
    public void getMetadataSuccess() throws Exception {
        HttpHeaders expectedHTTPHeaders = new HttpHeaders();
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY_OLD, AUTH_TOKEN);
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY, EXPECTED_AUTH_HEADER);
        HttpEntity expectedEntity = new HttpEntity<>(expectedHTTPHeaders);

        given(restTemplateMock.exchange(eq(METADATA_URL), eq(HttpMethod.GET), any(), eq(Metadata.class)))
                .willReturn(metadataResponseEntity);

        given(metadataResponseEntity.getStatusCode())
                .willReturn(HttpStatus.OK);

        given(metadataResponseEntity.getBody())
                .willReturn(metadata);

        Metadata result = api.getMetadata(VERSION_URI);

        assertThat("metadata does not match expected value", result, equalTo(metadata));
        verify(restTemplateMock, times(1)).exchange(eq(METADATA_URL), eq(HttpMethod.GET), eq(expectedEntity),
                eq(Metadata.class));
    }

    @Test(expected = FilterAPIException.class)
    public void getMetadataRestClientError() throws Exception {
        HttpHeaders expectedHTTPHeaders = new HttpHeaders();
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY_OLD, AUTH_TOKEN);
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY, EXPECTED_AUTH_HEADER);
        HttpEntity expectedEntity = new HttpEntity<>(null, expectedHTTPHeaders);

        try {
            given(restTemplateMock.exchange(eq(METADATA_URL), eq(HttpMethod.GET), any(), eq(Metadata.class)))
                    .willThrow(new RestClientException("spectaular explosion"));

            given(metadataResponseEntity.getStatusCode())
                    .willReturn(HttpStatus.OK);

            given(metadataResponseEntity.getBody())
                    .willReturn(metadata);

            api.getMetadata(VERSION_URI);
        } catch (RestClientException e) {
            verify(restTemplateMock, times(1)).exchange(eq(METADATA_URL), eq(HttpMethod.GET), eq(expectedEntity),
                    eq(Metadata.class));
            throw e;
        }
    }

    @Test(expected = MalformedURLException.class)
    public void getMetadataInvalidURLError() throws Exception {
        try {
            api.getMetadata("hello world");
        } catch (RestClientException e) {
            verify(restTemplateMock, never()).exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class),
                    eq(Metadata.class));
            throw e;
        }
    }

    @Test
    public void putDatasetVersionDownloadsSuccess() throws Exception {
        Download xlsx = new Download("/", "1234");
        DownloadsList downloadsList = new DownloadsList(xlsx, null);
        Version v = new Version(downloadsList);

        HttpHeaders expectedHTTPHeaders = new HttpHeaders();
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY_OLD, AUTH_TOKEN);
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY, EXPECTED_AUTH_HEADER);
        HttpEntity<Version> expectedEntity = new HttpEntity<>(v, expectedHTTPHeaders);

        given(restTemplateMock.exchange(eq(VERSION_URL), eq(HttpMethod.PUT), any(), eq(Void.class)))
                .willReturn(versionResponseEntity);

        given(versionResponseEntity.getStatusCode())
                .willReturn(HttpStatus.OK);

        api.putVersionDownloads(VERSION_URI, downloadsList);

        verify(restTemplateMock, times(1)).exchange(eq(VERSION_URL), eq(HttpMethod.PUT), eq(expectedEntity),
                eq(Void.class));
    }

    @Test(expected = FilterAPIException.class)
    public void putVersionDownloadsRestClientError() throws Exception {
        Download xlsx = new Download("/", "1234");
        DownloadsList downloadsList = new DownloadsList(xlsx, null);
        Version v = new Version(downloadsList);

        HttpHeaders expectedHTTPHeaders = new HttpHeaders();
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY_OLD, AUTH_TOKEN);
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY, EXPECTED_AUTH_HEADER);
        HttpEntity<Version> expectedEntity = new HttpEntity<>(v, expectedHTTPHeaders);

        given(restTemplateMock.exchange(eq(VERSION_URL), eq(HttpMethod.PUT), eq(expectedEntity), eq(Void.class)))
                .willThrow(new RestClientException("flubba wubba dub dub!"));

        try {
            api.putVersionDownloads(VERSION_URI, downloadsList);
        } catch (MalformedURLException e) {
            verify(restTemplateMock, times(1)).exchange(eq(VERSION_URL), eq(HttpMethod.PUT), eq(expectedEntity),
                    eq(Void.class));
            throw e;
        }
    }

    @Test(expected = FilterAPIException.class)
    public void putVersionDownloadsIncorrectHTTPResponseStatus() throws Exception {
        Download xlsx = new Download("/", "1234");
        DownloadsList downloadsList = new DownloadsList(xlsx, null);
        Version v = new Version(downloadsList);

        HttpHeaders expectedHTTPHeaders = new HttpHeaders();
        expectedHTTPHeaders.add(AUTH_TOKEN_KEY_OLD, AUTH_TOKEN);
        HttpEntity<Version> expectedEntity = new HttpEntity<>(v, expectedHTTPHeaders);

        given(restTemplateMock.exchange(eq(VERSION_URL), eq(HttpMethod.PUT), any(), eq(Void.class)))
                .willReturn(versionResponseEntity);

        given(versionResponseEntity.getStatusCode())
                .willReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            api.putVersionDownloads(VERSION_URI, downloadsList);
        } catch (MalformedURLException e) {
            verify(restTemplateMock, times(1)).exchange(eq(VERSION_URL), eq(HttpMethod.PUT), eq(expectedEntity),
                    eq(Void.class));
            throw e;
        }
    }
}
