package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;


public class DatasetFormatterTest {

    private final DatasetFormatter datasetFormatter = new DatasetFormatter();

    @Test
    public void createXlsxFormat() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {

            final V4File file = new V4File(stream);
            final Metadata datasetMetadata = new Metadata();
            final Workbook wb = new XSSFWorkbook();
            final CellStyle style = createStyle(wb);
            final Sheet sheet = wb.createSheet("Test");

            datasetFormatter.format(sheet, file, datasetMetadata, style, style);

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
            assertThat(sheet.getDefaultColumnWidth()).isEqualTo(8);
        }
    }

    @Test
    public void createXlsxWithEmptyObservation() throws IOException {

        // Given some v4 file data with an empty observation field
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = ",Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        final V4File file = new V4File(inputStream);
        final Metadata datasetMetadata = new Metadata();
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Test");

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style);

        // Then the empty observation value is in the output
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("");
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