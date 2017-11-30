package dp.xlsx;

import dp.api.dataset.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XLSXConverterTest {

    @Autowired
    private XLSXConverter converter;

    @Test
    public void csvToXlsx() throws IOException {
        try (final InputStream csv = XLSXConverterTest.class.getResourceAsStream("v4_0.csv")) {

            Metadata datasetMetadata = new Metadata();
            datasetMetadata.setTitle("test title");

            ByteArrayOutputStream xlxs = converter.toXLSX(csv, datasetMetadata);

            // For an XLSX file it seems impossible to recreate the same output, as to identical runs will not create
            // two identical files.
            assertThat(xlxs.toString().length()).isGreaterThan(4000).isLessThan(4200);
        }
    }
}
