package dp.handler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.vault.core.VaultTemplate;

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
import dp.api.dataset.models.Version;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.api.filter.FilterLinks;
import dp.avro.ExportedFile;
import dp.exceptions.FilterAPIException;
import dp.s3crypto.S3Crypto;
import dp.xlsx.CMDWorkbook;
import dp.xlsx.Converter;

@RunWith(SpringRunner.class)
@SpringBootTest
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

	@MockBean
	@Qualifier("crypto-client")
	private S3Crypto s3Crypto;

	@Configuration
	static class Config {

		@Bean
		VaultTemplate vaultTemplate() {
			return Mockito.mock(VaultTemplate.class);
		}

		@Bean
		Handler handler() {
			return new Handler();
		}
	}

	@MockBean
	private SessionManager sessionManager;

	@Mock
	private CMDWorkbook workbookMock;

	@Autowired
	private Handler handler;

	private String instanceID = "123";
	private String datasetID = "456";
	private String edition = "2017";
	private String version = "1";
	private String filename = "morty";
	private String versionURL = "/datasets/456/editions/2017/versions/1";

	@Test
	public void validExportFileFilterMessage() throws IOException {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
		ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);

		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

		Filter filter = createFilter();

		when(filterAPI.getFilter(any())).thenReturn(filter);

		Metadata datasetMetadata = new Metadata();
		when(datasetAPI.getMetadata(filter.getLinks().getVersion().getHref())).thenReturn(datasetMetadata);

		when(converter.toXLSX(any(), any())).thenReturn(workbookMock);

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
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

		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
		when(filterAPI.getFilter(any())).thenReturn(filter);

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, never()).getMetadata(anyString());
		verify(converter, never()).toXLSX(any(), any());
		verify(s3Client, never()).putObject(any());
		verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
	}

	@Test
	public void validFilterMessageGetMetadataError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

		Filter filter = createFilter();

		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
		when(filterAPI.getFilter(any())).thenReturn(filter);
		when(datasetAPI.getMetadata(versionURL)).thenThrow(new FilterAPIException("flubba wubba dub dub", null));

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);
		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, never()).toXLSX(any(), any());
		verify(s3Client, never()).putObject(any());
		verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
	}

	@Test
	public void validFilterConverterError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

		Filter filter = createFilter();
		Metadata datasetMetadata = new Metadata();
		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
		when(filterAPI.getFilter(any())).thenReturn(filter);
		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), eq(datasetMetadata))).thenThrow(new IOException());

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, times(1)).toXLSX(any(), eq(datasetMetadata));
		verify(filterAPI, never()).addXLSXFile(any(), any(), anyLong());
		verify(s3Client, never()).putObject(any());
	}

	@Test
	public void validFilterMessageS3PutError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
		Workbook workbookMock = mock(Workbook.class);
		SdkClientException ex = mock(SdkClientException.class);
		ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

		Filter filter = createFilter();
		Metadata datasetMetadata = new Metadata();

		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
		when(filterAPI.getFilter(any())).thenReturn(filter);
		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), eq(datasetMetadata))).thenReturn(workbookMock);
		when(s3Client.putObject(any())).thenThrow(ex);

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
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
		Workbook workbookMock = mock(Workbook.class);
		JsonProcessingException ex = mock(JsonProcessingException.class);
		ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

		Filter filter = createFilter();
		Metadata datasetMetadata = new Metadata();

		Version version = new Version();
		version.setState("published");

		when(datasetAPI.getVersion("/instances/12345")).thenReturn(version);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));
		when(filterAPI.getFilter(any())).thenReturn(filter);
		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), eq(datasetMetadata))).thenReturn(workbookMock);
		when(s3Client.putObject(any())).thenReturn(null);
		doThrow(ex).when(filterAPI).addXLSXFile(any(), any(), anyLong());

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "12345", "", "", "", "");

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject(anyString(), anyString());
		verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
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
		ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);

		Metadata datasetMetadata = new Metadata();

		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/morty.xlsx"));
		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), any())).thenReturn(workBookMock);

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, times(1)).toXLSX(any(), any());
		verify(datasetAPI, times(1)).putVersionDownloads(any(), any());
		verify(workBookMock, times(1)).write(any(OutputStream.class));
		verify(s3Client, times(1)).putObject(arguments.capture());

		assertThat("inccorrect bucket name", arguments.getValue().getBucketName(), equalTo("csv-exported"));
		assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("morty.xlsx"));
	}

	@Test
	public void vaildPrePublishMessageGetObjectError() throws Exception {
		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);
		when(s3Client.getObject("bucket", "v4.csv")).thenThrow(mock(SdkClientException.class));

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject("bucket", "v4.csv");
		verify(datasetAPI, never()).getMetadata(versionURL);
		verify(converter, never()).toXLSX(any(), any());
		verify(datasetAPI, never()).putVersionDownloads(any(), any());
		verify(s3Client, never()).putObject(any());
	}

	@Test
	public void vaildPrePublishMessageGetMetadataError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		
		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);

		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(datasetAPI.getMetadata(anyString())).thenThrow(new FilterAPIException("flubba wubba dub dub", null));

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject("bucket", "v4.csv");
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, never()).toXLSX(any(), any());
		verify(datasetAPI, never()).putVersionDownloads(any(), any());
		verify(s3Client, never()).putObject(any());
	}

	@Test
	public void vaildPrePublishMessageConvertToXLSError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
		Metadata metadata = new Metadata();
		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(datasetAPI.getMetadata(versionURL)).thenReturn(metadata);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(converter.toXLSX(stream, metadata)).thenThrow(new IOException());

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject("bucket", "v4.csv");
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

		when(datasetAPI.getVersion("/instances/123")).thenThrow(new MalformedURLException());

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, never()).getObject("bucket", "v4.csv");
		verify(s3Object, never()).getObjectContent();
		verify(datasetAPI, never()).getMetadata(versionURL);
		verify(converter, never()).toXLSX(stream, metadata);
		verify(datasetAPI, never()).putVersionDownloads(any(), any());
		verify(s3Client, never()).putObject(any());
	}

	@Test
	public void vaildPrePublishMessagePutVersionDatasetAPIError() throws Exception {
		S3Object s3Object = mock(S3Object.class);
		S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
		ArgumentCaptor<PutObjectRequest> arguments = ArgumentCaptor.forClass(PutObjectRequest.class);
		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);

		DownloadsList downloads = new DownloadsList(new Download("https://amazon.com/morty.xlsx", "0"), null);

		Metadata metadata = new Metadata();

		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/morty.xlsx"));
		when(datasetAPI.getMetadata(versionURL)).thenReturn(metadata);
		when(converter.toXLSX(any(), any())).thenReturn(workbookMock);
		doThrow(new FilterAPIException("flubba wubba dub dub", null)).when(datasetAPI)
				.putVersionDownloads(eq(versionURL), any());

		final ExportedFile exportedFile = new ExportedFile("", "s3://bucket/v4.csv", instanceID, datasetID, edition,
				version, filename);

		handler.listen(exportedFile);

		verify(datasetAPI, times(1)).getVersion(anyString());
		verify(s3Client, times(1)).getObject("bucket", "v4.csv");
		verify(s3Object, times(1)).getObjectContent();
		verify(datasetAPI, times(1)).getMetadata(versionURL);
		verify(converter, times(1)).toXLSX(stream, metadata);
		verify(datasetAPI, times(1)).putVersionDownloads(eq(versionURL), any());
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
		Version ver = new Version();
		ver.setState("published");

		when(datasetAPI.getVersion("/instances/123")).thenReturn(ver);
		when(s3Object.getObjectContent()).thenReturn(stream);
		when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
		when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

		Filter filter = createFilter();
		when(filterAPI.getFilter(any())).thenReturn(filter);

		Metadata datasetMetadata = new Metadata();

		when(datasetAPI.getMetadata(versionURL)).thenReturn(datasetMetadata);
		when(converter.toXLSX(any(), any())).thenReturn(workbookMock);
		when(s3Client.putObject(any())).thenThrow(new RuntimeException());

		final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv", "", "", "", "", "");

		try {
			handler.listen(exportedFile);
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
			assertThat("inccorrect filename", arguments.getValue().getKey(), equalTo("123.xlsx"));
		}
	}

	private Filter createFilter() {
		Filter filter = new Filter();
		FilterLinks filterLinks = new FilterLinks();
		Link versionLink = new Link();
		String versionHref = "http://localhost:22000/datasets/456/editions/2017/versions/1";
		versionLink.setHref(versionHref);
		versionLink.setId("666");
		filterLinks.setVersion(versionLink);
		filter.setLinks(filterLinks);
		return filter;
	}

}
