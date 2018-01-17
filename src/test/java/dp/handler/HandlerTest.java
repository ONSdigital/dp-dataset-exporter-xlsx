package dp.handler;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import dp.api.Link;
import dp.api.dataset.DatasetAPIClientImpl;
import dp.api.dataset.models.Download;
import dp.api.dataset.models.DownloadsList;
import dp.api.dataset.models.Metadata;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.api.filter.FilterLinks;
import dp.avro.ExportedFile;
import dp.exceptions.FilterAPIException;
import dp.xlsx.CMDWorkbook;
import dp.xlsx.Converter;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HandlerTest {

    @MockBean
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
    private Handler handler;

    private String instanceID = "123";
    private String datasetID = "456";
    private String edition = "2017";
    private String version = "1";
    private String filename = "morty";
    private String metadataURL = "/datasets/456/editions/2017/versions/1";

    @Test
    public void validExportFileFilterMessage() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

        Filter filter = createFilter();
        when(filterAPI.getFilter(any())).thenReturn(filter);

        Metadata datasetMetadata = new Metadata();
        when(datasetAPI.getMetadata(filter.getLinks().getVersion().getHref())).thenReturn(datasetMetadata);

        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(new URL(filter.getLinks().getVersion().getHref() + "/metadata"));
        verify(converter, times(1)).toXLSX(any(), any());

        verify(s3Client, times(1)).putObject(any());
        verify(s3Client, times(1)).putObject(arguments.capture());

        assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("123.xlsx"));
    }

    @Test
    public void filterLinkInvalidError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        Filter filter = createFilter();
        filter.getLinks().getVersion().setHref("this is not a link");

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any()))
                .thenReturn(filter);

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, never()).getMetadata(any(URL.class));
        verify(converter, never()).toXLSX(any(), any());
        verify(s3Client, never()).putObject(any());
        verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
    }

    @Test
    public void validFilterMessageGetMetadataError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        URL metadataURL = new URL("http://localhost:20000/filters/1/metadata");

        Filter filter = createFilter();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any()))
                .thenReturn(filter);
        when(datasetAPI.getMetadata(metadataURL))
                .thenThrow(new FilterAPIException("flubba wubba dub dub", null));

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);
        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(s3Client, never()).putObject(any());
        verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
    }

    @Test
    public void validFilterConverterError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        URL metadataURL = new URL("http://localhost:20000/filters/1/metadata");

        Filter filter = createFilter();
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any()))
                .thenReturn(filter);
        when(datasetAPI.getMetadata(metadataURL))
                .thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata)))
                .thenThrow(new IOException());

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void validFilterMessageS3PutError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        URL metadataURL = new URL("http://localhost:20000/filters/1/metadata");
        Workbook workbookMock = mock(Workbook.class);
        SdkClientException ex = mock(SdkClientException.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        Filter filter = createFilter();
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any()))
                .thenReturn(filter);
        when(datasetAPI.getMetadata(metadataURL))
                .thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata)))
                .thenReturn(workbookMock);
        when(s3Client.putObject(any()))
                .thenThrow(ex);

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());

        assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("123.xlsx"));
    }

    @Test
    public void validFilterMessageFilterAPIAddXLSFileError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        URL metadataURL = new URL("http://localhost:20000/filters/1/metadata");
        Workbook workbookMock = mock(Workbook.class);
        JsonProcessingException ex = mock(JsonProcessingException.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        Filter filter = createFilter();
        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/sdfsdf"));
        when(filterAPI.getFilter(any()))
                .thenReturn(filter);
        when(datasetAPI.getMetadata(metadataURL))
                .thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), eq(datasetMetadata)))
                .thenReturn(workbookMock);
        when(s3Client.putObject(any()))
                .thenReturn(null);
        doThrow(ex).when(filterAPI)
                .addXLSXFile(any(), any(), anyLong());

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(filterAPI, times(1)).addXLSXFile(any(), any(), anyLong());

        assertThat("inccorrect buck name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("123.xlsx"));
    }

    @Test
    public void vaildExportFilePrePublishMessage() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        SXSSFWorkbook workBookMock = mock(SXSSFWorkbook.class);

        DownloadsList downloads = new DownloadsList(new Download("https://amazon.com/morty.xlsx", "0"), null);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);


        Metadata datasetMetadata = new Metadata();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/morty.xlsx"));
        when(datasetAPI.getMetadata(metadataURL))
                .thenReturn(datasetMetadata);
        when(converter.toXLSX(any(), any()))
                .thenReturn(workBookMock);

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename);

        handler.listen(exportedFile);

        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(any(), any());
        verify(datasetAPI, times(1)).putVersionDownloads(metadataURL, downloads);
        verify(workBookMock, times(1)).write(any(OutputStream.class));
        verify(s3Client, times(1)).putObject(arguments.capture());

        assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("morty.xlsx"));
    }

    @Test
    public void vaildPrePublishMessageGetObjectError() throws Exception {
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenThrow(mock(SdkClientException.class));

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename);

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject("bucket", "v4.csv");
        verify(datasetAPI, never()).getMetadata(metadataURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void vaildPrePublishMessageGetMetadataError() throws Exception {
        S3Object s3Object = mock(S3Object.class);

        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(datasetAPI.getMetadata(anyString()))
                .thenThrow(new FilterAPIException("flubba wubba dub dub", null));

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename);

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject("bucket", "v4.csv");
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, never()).toXLSX(any(), any());
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void vaildPrePublishMessageConvertToXLSError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        Metadata metadata = new Metadata();

        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(datasetAPI.getMetadata(anyString()))
                .thenReturn(metadata);
        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(converter.toXLSX(stream, metadata))
                .thenThrow(new IOException());

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename);

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject("bucket", "v4.csv");
        verify(s3Object, times(1)).getObjectContent();
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(stream, metadata);
        verify(datasetAPI, never()).putVersionDownloads(any(), any());
        verify(s3Client, never()).putObject(any());
    }

    @Test
    public void vaildPrePublishMessagePutVersionDatasetAPIError() throws Exception {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        DownloadsList downloads = new DownloadsList(new Download("https://amazon.com/morty.xlsx", "0"), null);

        Metadata metadata = new Metadata();

        when(s3Object.getObjectContent())
                .thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv"))
                .thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString()))
                .thenReturn(new URL("https://amazon.com/morty.xlsx"));
        when(datasetAPI.getMetadata(metadataURL))
                .thenReturn(metadata);
        when(converter.toXLSX(any(), any()))
                .thenReturn(workbookMock);
        doThrow(new FilterAPIException("flubba wubba dub dub", null)).when(datasetAPI)
                .putVersionDownloads(any(), any());

        final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
                version, filename);

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject("bucket", "v4.csv");
        verify(s3Object, times(1)).getObjectContent();
        verify(datasetAPI, times(1)).getMetadata(metadataURL);
        verify(converter, times(1)).toXLSX(stream, metadata);
        verify(datasetAPI, times(1)).putVersionDownloads(any(), any());
        verify(s3Client, times(1)).putObject(arguments.capture());
        verify(workbookMock, times(1)).close();

        assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
        assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("morty.xlsx"));
    }

    @Test
    public void testPutS3ClientError() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

        Filter filter = createFilter();
        when(filterAPI.getFilter(any())).thenReturn(filter);

        Metadata datasetMetadata = new Metadata();
        when(datasetAPI.getMetadata(filter.getLinks().getVersion().getHref())).thenReturn(datasetMetadata);

        when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

        when(s3Client.putObject(any()))
                .thenThrow(new RuntimeException());

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

        try {
            handler.listen(exportedFile);
        } catch (Exception e) {
            verify(s3Client, times(1)).getObject(anyString(), anyString());
            verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
            verify(datasetAPI, times(1)).getMetadata(filter.getLinks().getVersion().getHref());
            verify(converter, times(1)).toXLSX(any(), any());
            verify(s3Client, times(1)).putObject(arguments.capture());
            verify(workbookMock, times(1)).dispose();
            verify(workbookMock, times(1)).close();

            assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
            assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("123.xlsx"));
        }
    }

    private Filter createFilter() {
        Filter filter = new Filter();
        FilterLinks filterLinks = new FilterLinks();
        Link versionLink = new Link();
        String versionHref = "http://localhost:20000/filters/1";
        versionLink.setHref(versionHref);
        versionLink.setId("666");
        filterLinks.setVersion(versionLink);
        filter.setLinks(filterLinks);
        return filter;
    }

}
