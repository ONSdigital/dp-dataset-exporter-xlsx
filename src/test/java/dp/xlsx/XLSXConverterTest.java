package dp.xlsx;

import dp.api.dataset.models.Metadata;
import org.apache.poi.ss.usermodel.Workbook;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;


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

            Workbook workbook = converter.toXLSX(csv, datasetMetadata);

            Assertions.assertThat(workbook.getNumberOfSheets()).isEqualTo(2);
        }
    }
}
