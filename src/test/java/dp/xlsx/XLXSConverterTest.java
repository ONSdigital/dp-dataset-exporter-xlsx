package dp.xlsx;

import dp.dataset.Dataset;
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
public class XLXSConverterTest {
    @Autowired
    private XLXSConverter converter;

    @Test
    public void csvToXlsx() throws IOException {
        final Dataset dataset = new Dataset();
        dataset.setId("1234");
        dataset.setTitle("CPI October 2017");

        try (final InputStream csv = XLXSConverterTest.class.getResourceAsStream("v4_0.csv");
             final InputStream results = XLXSConverterTest.class.getResourceAsStream("expected_results.xlsx")) {
            ByteArrayOutputStream xlxs = converter.toXLXS(dataset, csv);
            // For an xlsx file it seems to have some time / uuid in the file format. This makes it difficult to do
            // any binary comparision. For a single test we just check that the file length is between to values.
            assertThat(xlxs.toString().length()).isGreaterThan(3731).isLessThan(3770);
        }

    }
}
