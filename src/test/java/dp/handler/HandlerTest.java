package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import dp.api.Link;
import dp.api.dataset.DatasetAPIClient;
import dp.api.dataset.Metadata;
import dp.api.filter.Filter;
import dp.api.filter.FilterAPIClient;
import dp.api.filter.FilterLinks;
import dp.avro.ExportedFile;
import dp.xlsx.XLSXConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HandlerTest {

    @MockBean
    private AmazonS3 s3Client;

    @MockBean
    private XLSXConverter converter;

    @MockBean
    private FilterAPIClient filterAPI;

    @MockBean
    private DatasetAPIClient datasetAPI;

    @Autowired
    private Handler handler;

    @Test
    public void validExportFileMessage() throws IOException {

        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);

        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL("https://amazon.com/sdfsdf"));

        Filter filter = createFilter();
        when(filterAPI.getFilter(any())).thenReturn(filter);

        Metadata datasetMetadata = new Metadata();
        when(datasetAPI.getMetadata(filter.getLinks().getVersion().getHref())).thenReturn(datasetMetadata);

        when(converter.toXLSX(any(), any())).thenReturn(new XSSFWorkbook());

        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv");

        handler.listen(exportedFile);

        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(filterAPI, times(1)).getFilter(exportedFile.getFilterId().toString());
        verify(datasetAPI, times(1)).getMetadata(filter.getLinks().getVersion().getHref());
        verify(converter, times(1)).toXLSX(any(), any());
        verify(s3Client, times(1)).putObject(any());
    }

    private Filter createFilter() {
        Filter filter = new Filter();
        FilterLinks filterLinks = new FilterLinks();
        Link versionLink = new Link();
        String versionHref = "localhost:20000/filters/1";
        versionLink.setHref(versionHref);
        filterLinks.setVersion(versionLink);
        filter.setLinks(filterLinks);
        return filter;
    }

}
