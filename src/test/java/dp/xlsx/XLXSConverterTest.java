package dp.xlsx;

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
        try (final InputStream csv = XLXSConverterTest.class.getResourceAsStream("v4_0.csv")) {
            ByteArrayOutputStream xlxs = converter.toXLXS(csv);
            // For an XLSX file it seems impossible to recreate the same output, as to identical runs will not create
            // two identical files.
            assertThat(xlxs.toString().length()).isGreaterThan(3500).isLessThan(3770);
        }
    }
}
