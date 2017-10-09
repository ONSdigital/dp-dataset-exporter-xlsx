package dp.xlsx;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;


public class FormatterTest {

    @Test
    public void createXlsxFormat() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream);
            final Workbook wb = new XSSFWorkbook();
            final CellStyle style = createStyle(wb);
            final Sheet sheet = wb.createSheet("Test");
            Formatter formatter = new Formatter();
            formatter.format(sheet, file, style, style);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
            assertThat(sheet.getDefaultColumnWidth()).isEqualTo(8);
        }
    }

    private CellStyle createStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }
}
