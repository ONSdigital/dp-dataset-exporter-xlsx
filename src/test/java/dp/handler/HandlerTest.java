package dp.handler;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import dp.api.Link;
import dp.api.dataset.DatasetAPIClientImpl;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.Version;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.api.filter.FilterLinks;
import dp.avro.ExportedFile;
import dp.configuration.TestConfig;
import dp.exceptions.FilterAPIException;
import dp.xlsx.CMDWorkbook;
import dp.xlsx.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
public class HandlerTest {

    @MockBean
    @Qualifier("s3-client")
    private AmazonS3 s3Client;

    @MockBean
    private Converter converter;

    @MockBean
    private FilterAPIClient filterAPI;

    @MockBean
    private DatasetAPIClientImpl datasetAPI;

    @Mock
    private CMDWorkbook workbookMock;

    @Autowired
    @InjectMocks
    private Handler handler;

    @Mock
    private Acknowledgment ack;

    private String instanceID = "inst123";
    private String datasetID = "ds456";
    private String edition = "2017";
    private String version = "1";
    private String filename = "morty";
    private String versionURL = "/datasets/ds456/editions/2017/versions/1";
    private Integer rowCount = 20000;
    private String bucketURL = "bucket";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validFullDownloadWithNonPublishedState() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<DownloadsList> downLoadArguments = ArgumentCaptor.forClass(DownloadsList.class);

        Metadata datasetMetadata = new Metadata();

        Version ver = new Version();
        ver.setState("associated");

        Map<String, Object> map = new HashMap<>();
        map.put("key", "746573742D6B6579");

        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "datasets/v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/datasets/morty.xlsx"));
        when(datasetAPI.getMetadata("/instances/inst123")).thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/datasets/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion("/instances/" + instanceID);
        verify(datasetAPI, times(1)).putVersionDownloads(any(), downLoadArguments.capture());
        verify(workbookMock, times(1)).write(any(OutputStream.class));
        verify(s3Client, times(1)).putObject(any());
        verify(s3Client, times(1)).putObject(any());
        verify(converter, times(1)).toXLSX(any(), any());

        assertThat("public URL should be empty", downLoadArguments.getValue().getXls().getPublicState(), equalTo(null));
    }

    @Test
    public void usesS3uri() throws Exception {
        handler.setBucketUrl("http://bucket");
        handler.setBucketS3Url("s3://s3-bucket-url");

        String s3uri = handler.getS3URL("http://bucket/test");

        assertThat("correctly uses the bucketS3URL value", s3uri, equalTo("s3://s3-bucket-url/test"));

        handler.setBucketS3Url("");
        handler.setBucketUrl("");
    }

    @Test
    public void doesNotUseS3uri() throws Exception {
        String s3uri = handler.getS3URL("test-bucket");

        assertThat("correctly avoids empty bucketS3URL value", s3uri, equalTo("test-bucket"));
    }

    @Test
    public void validFullDownloadWithPublishedStateAndBucketUrl() throws Exception {
        S3Object s3Object = mock(S3Object.class);

        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<DownloadsList> downLoadArguments = ArgumentCaptor.forClass(DownloadsList.class);

        Metadata datasetMetadata = new Metadata();

        Version ver = new Version();
        ver.setState("published");

        when(datasetAPI.getVersion("/instances/instbuckUrl")).thenReturn(ver);
        when(datasetAPI.getMetadata("/datasets/dsbuckUrl/editions/2017/versions/1")).thenReturn(datasetMetadata);
        when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/datasets/buckUrl.xlsx"));
        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("", "s3://booket/datasets/buckUrl.csv",
                "instbuckUrl", "dsbuckUrl", edition, version, "filenamebuckUrl", rowCount);

        handler.setBucketUrl("https://not-empty");
        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion("/instances/instbuckUrl");
        verify(datasetAPI, times(1)).getMetadata("/datasets/dsbuckUrl/editions/2017/versions/1");
        verify(workbookMock, times(1)).write(any(OutputStream.class));

        verify(converter, times(1)).toXLSX(any(), any());
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(datasetAPI, times(1)).putVersionDownloads(any(), downLoadArguments.capture());

        assertThat("incorrect public URL", downLoadArguments.getValue().getXls().getPublicState(), equalTo("https://not-empty/full-datasets/filenamebuckUrl.xlsx"));
        assertThat("incorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), equalTo("full-datasets/filenamebuckUrl.xlsx"));

        handler.setBucketUrl("");
    }

    @Test
    public void validFilterWithPublishedStateAndBucketUrl() throws Exception {
        S3Object s3Object = mock(S3Object.class);

        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<String> xlsArguments = ArgumentCaptor.forClass(String.class);

        Metadata datasetMetadata = new Metadata();

        boolean published = true;
        Filter filter = createFilter(published);
        when(filterAPI.getFilter(any())).thenReturn(filter);

        when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/datasets/filtbuckUrl.xlsx"));
        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("filter-id-1212", "s3://booket/datasets/filtbuckUrl.csv",
                                "instFiltbuckUrl", "dsFiltbuckUrl", edition, version, "filenameFiltbuckUrl.xlsx", rowCount);

        handler.setBucketUrl("https://not-empty");
        handler.listen(exportedFile, ack);

        verify(workbookMock, times(1)).write(any(OutputStream.class));

        verify(converter, times(1)).toXLSX(any(), any());
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(filterAPI, times(1)).addXLSXFile(any(), any(), xlsArguments.capture(), anyLong(), anyBoolean());

        assertThat("incorrect public URL", xlsArguments.getValue(), containsString("https://not-empty/filtered-datasets/filter-id-1212/dsFiltbuckUrl-2017-v1-filtered-"));
        assertThat("incorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), containsString("filtered-datasets/filter-id-1212/dsFiltbuckUrl-2017-v1-filtered-"));

        handler.setBucketUrl("");
    }

    @Test
    public void validFullDownloadTooLarge() throws Exception {
        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/datasets/v4.csv", instanceID, datasetID, edition,
                version, filename, 500000000);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, never()).getVersion("/instances/" + instanceID);
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(converter, never()).toXLSX(any(), any());
    }

    @Test
    public void validExportFileFilterMessage() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

        boolean published = true;
        Filter filter = createFilter(published);
        when(filterAPI.getFilter(any())).thenReturn(filter);

        Metadata datasetMetadata = new Metadata();
        when(datasetAPI.getMetadata(filter.getLinks().getVersion().getHref())).thenReturn(datasetMetadata);

        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "cpih", "2018", "1", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(any(), any());

        verify(s3Client, times(1)).putObject(any());
        verify(s3Client, times(1)).putObject(arguments.capture());

        assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), containsString("filtered-datasets/inst123/cpih-2018-v1-filtered"));
    }

    @Test
    public void validFilterTooLarge() throws IOException {
        boolean published = true;
        Filter filter = createFilter(published);
        when(filterAPI.getFilter(any())).thenReturn(filter);

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "", "", "", "", 500000000);

        handler.listen(exportedFile, ack);

        verify(filterAPI, never()).getFilter(exportedFile.getFilterId().toString());
        verify(filterAPI, times(1)).setToComplete(exportedFile.getFilterId().toString());
        verify(datasetAPI, never()).getMetadata(versionURL);
        verify(converter, never()).toXLSX(any(), any());
    }

    @Test
    public void filterLinkInvalidError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        boolean published = true;
        Filter filter = createFilter(published);
        filter.getLinks().getVersion().setHref("this is not a link");

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any())).thenReturn(filter);

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "", "", "", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, never()).getMetadata(anyString());
        verify(converter, never()).toXLSX(any(), any());
        verify(s3Client, never()).putObject(any());
        verify(filterAPI, never()).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());
    }

    @Test
    public void validFilterMessageGetMetadataError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        boolean published = true;
        Filter filter = createFilter(published);

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any())).thenReturn(filter);
        when(datasetAPI.getMetadata(versionURL)).thenThrow(new FilterAPIException("flubba wubba dub dub", null));

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "", "", "", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(s3Client, never()).putObject(any());
        verify(filterAPI, never()).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());
    }

    @Test
    public void validFilterConverterError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        boolean published = true;
        Filter filter = createFilter(published);
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any())).thenReturn(filter);
        when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata))).thenThrow(new IOException());

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "", "", "", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(filterAPI, never()).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validFilterMessageS3PutError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        SdkClientException ex = mock(SdkClientException.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        boolean published = true;
        Filter filter = createFilter(published);
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any())).thenReturn(filter);
        when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata))).thenReturn(workbookMock);
        when(s3Client.putObject(any())).thenThrow(ex);

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "ASHE-8", "2019", "1", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(filterAPI, never()).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());

        assertThat("incorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), containsString("filtered-datasets/inst123/ASHE-8-2019-v1-filtered-"));
    }

    @Test
    public void validFilterMessageFilterAPIAddXLSFileError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        JsonProcessingException ex = mock(JsonProcessingException.class);
        StackTraceElement[] s = {new StackTraceElement("class", "method", "filename", 1)};
        when(ex.getStackTrace()).thenReturn(s);

        boolean published = true;
        Filter filter = createFilter(published);
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any())).thenReturn(filter);
        when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata))).thenReturn(workbookMock);
        when(s3Client.putObject(any())).thenReturn(null);
        doThrow(ex).when(filterAPI).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "12345", "cpih01", "2020", "11", "", rowCount);

        handler.listen(exportedFile, ack);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(filterAPI, times(1)).addXLSXFile(any(), any(), any(), anyLong(), anyBoolean());

        assertThat("incorrect buck name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), containsString("filtered-datasets/inst123/cpih01-2020-v11-filtered-"));
    }

    @Test
    public void validExportFilePrePublishMessage() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        Metadata datasetMetadata = new Metadata();

		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/morty.xlsx"));
		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, times(1)).toXLSX(any(), any());
		verify(datasetAPI, times(1)).putVersionDownloads(any(), any());
		verify(workbookMock, times(1)).write(any(OutputStream.class));
		verify(s3Client, times(1)).putObject(arguments.capture());

        assertThat("incorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), equalTo("full-datasets/morty.xlsx"));
    }

    @Test
    public void validPrePublishMessageGetObjectError() throws Exception {
        Version ver = new Version();
        ver.setState("published");

        SdkClientException mockSdkClientException = mock(SdkClientException.class);
        StackTraceElement[] s = {new StackTraceElement("class", "method", "filename", 1)};
        when(mockSdkClientException.getStackTrace()).thenReturn(s);
        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenThrow(mockSdkClientException);

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion(anyString());
        verify(s3Client, times(1)).getObject(bucketURL, "v4.csv");
        verify(datasetAPI, never()).getMetadata(versionURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validPrePublishMessageGetMetadataError() throws Exception {
        S3Object s3Object = mock(S3Object.class);

        Version ver = new Version();
        ver.setState("published");

        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);

        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(datasetAPI.getMetadata(anyString())).thenThrow(new FilterAPIException("flubba wubba dub dub", null));

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion(anyString());
        verify(s3Client, times(1)).getObject(bucketURL, "v4.csv");
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validPrePublishMessageConvertToXLSError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        Metadata metadata = new Metadata();
        Version ver = new Version();
        ver.setState("published");

        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(datasetAPI.getMetadata(versionURL)).thenReturn(metadata);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(converter.toXLSX(stream, metadata)).thenThrow(new IOException());

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion(anyString());
        verify(s3Client, times(1)).getObject(bucketURL, "v4.csv");
        verify(s3Object, times(1)).getObjectContent();
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(stream, metadata);
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validMessageWithVersionGetError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        Metadata metadata = new Metadata();

        when(datasetAPI.getVersion("/instances/inst123")).thenThrow(new MalformedURLException());

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion(anyString());
        verify(s3Client, never()).getObject(bucketURL, "v4.csv");
        verify(s3Object, never()).getObjectContent();
        verify(datasetAPI, never()).getMetadata(versionURL);
        verify(converter, never()).toXLSX(stream, metadata);
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validPrePublishMessagePutVersionDatasetAPIError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        Version ver = new Version();
        ver.setState("published");

        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);

        Metadata metadata = new Metadata();

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/morty.xlsx"));
        when(datasetAPI.getMetadata(versionURL)).thenReturn(metadata);
        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);
        doThrow(new FilterAPIException("flubba wubba dub dub", null)).when(datasetAPI)
                .putVersionDownloads(eq(versionURL), any());

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename, rowCount);

        handler.listen(exportedFile, ack);

        verify(datasetAPI, times(1)).getVersion(anyString());
        verify(s3Client, times(1)).getObject(bucketURL, "v4.csv");
        verify(s3Object, times(1)).getObjectContent();
        verify(datasetAPI, times(1)).getMetadata(versionURL);
        verify(converter, times(1)).toXLSX(stream, metadata);
        verify(datasetAPI, times(1)).putVersionDownloads(eq(versionURL), any());
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(workbookMock, times(1)).close();

        assertThat("incorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("incorrect filename", arguments.getValue().getKey(), equalTo("full-datasets/morty.xlsx"));
    }

    @Test
    public void testPutS3ClientError() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
        Version ver = new Version();
        ver.setState("published");

        when(datasetAPI.getVersion("/instances/inst123")).thenReturn(ver);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject(bucketURL, "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

        boolean published = true;
        Filter filter = createFilter(published);
        when(filterAPI.getFilter(any())).thenReturn(filter);

        Metadata datasetMetadata = new Metadata();

        when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);
        when(s3Client.putObject(any())).thenThrow(new RuntimeException());

        final ExportedFile exportedFile = new ExportedFile("inst123", "s3://bucket/v4.csv", "", "", "", "", "", rowCount);

        try {
            handler.listen(exportedFile, ack);
        } catch (Exception e) {
            verify(datasetAPI, times(1)).getVersion(anyString());
            verify(s3Client, times(1)).getObject(anyString(), anyString());
            verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
            verify(datasetAPI, times(1)).getMetadata(versionURL);
            verify(converter, times(1)).toXLSX(any(), any());
            verify(s3Client, times(1)).putObject(arguments.capture());
            verify(workbookMock, times(1)).dispose();
            verify(workbookMock, times(1)).close();

            assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
            assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("filtered-datasets/inst123.xlsx"));
        }
    }

    private Filter createFilter(boolean published) {
        Filter filter = new Filter();
        FilterLinks filterLinks = new FilterLinks();
        Link versionLink = new Link();
        String versionHref = "http://localhost:22000/datasets/ds456/editions/2017/versions/1";
        versionLink.setHref(versionHref);
        versionLink.setId("666");
        filterLinks.setVersion(versionLink);
        filter.setLinks(filterLinks);
        filter.setPublished(published);
        return filter;
    }
}
