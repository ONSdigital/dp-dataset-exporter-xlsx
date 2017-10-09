package dp.handler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import dp.avro.ExportedFile;
import dp.xlsx.XLXSConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HandlerTest {

    @MockBean
    private AmazonS3 s3Client;

    @MockBean
    private XLXSConverter converter;

    @Autowired
    private Handler handler;

    @Test
    public void validExportFileMessage() throws IOException {
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream stream = mock(S3ObjectInputStream.class);
        when(s3Object.getObjectContent()).thenReturn(stream);
        when(s3Client.getObject("bucket", "v4.csv")).thenReturn(s3Object);
        when(converter.toXLXS(any())).thenReturn(new ByteArrayOutputStream());
        final ExportedFile exportedFile = new ExportedFile("123", "s3://bucket/v4.csv");
        handler.listen(exportedFile);
        verify(s3Client, times(1)).getObject(anyString(), anyString());
        verify(converter, times(1)).toXLXS(any());
        verify(s3Client, times(1)).putObject(any());
    }

}
