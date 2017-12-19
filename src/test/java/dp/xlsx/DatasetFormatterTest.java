package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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
    private static final int metadataRows = 2;

    final Metadata datasetMetadata = new Metadata();
    final Workbook wb = new XSSFWorkbook();
    final CellStyle numberStyle = createNumberStyle(wb);
    final CellStyle style = createStyle(wb);
    final Sheet sheet = wb.createSheet("Test");

    @Test
    public void timeValuesAreOrderedAlphabeticallyWhenUnrecognised() throws IOException {

        // Given some V4 input data
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "45.2,Month,January-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "86.9,Month,February-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

        assertThat(sheet.getRow(metadataRows + 0).getCell(1).getStringCellValue()).isEqualTo("February-96");
        assertThat(sheet.getRow(metadataRows + 0).getCell(2).getStringCellValue()).isEqualTo("January-96");
    }

    @Test
    public void timeValuesAreOrderedChronologicallyWhenRecognised() throws IOException {

        // Given some V4 input data
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "45.2,Month,Jan-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "86.9,Month,Feb-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

        assertThat(sheet.getRow(metadataRows + 0).getCell(1).getStringCellValue()).isEqualTo("Jan-96");
        assertThat(sheet.getRow(metadataRows + 0).getCell(2).getStringCellValue()).isEqualTo("Feb-96");
    }

    @Test
    public void dimensionValuesAreOrderedAlphabetically() throws IOException {

        // Given some V4 input data
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "45.2,Month,Jan-96,K02000002,Wales,cpi1dim1A1,AAA\n";
        String csvRow2 = "86.9,Month,Jan-96,K02000003,England,cpi1dim1A2,BBB\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

        assertThat(sheet.getRow(metadataRows + 1).getCell(0).getStringCellValue()).isEqualTo("AAA\nWales (K02000002)");
        assertThat(sheet.getRow(metadataRows + 2).getCell(0).getStringCellValue()).isEqualTo("BBB\nEngland (K02000003)");
    }

    @Test
    public void format() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {

            final V4File file = new V4File(stream);

            datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 7);
        }
    }

    @Test
    public void format_OutputsMetadata() throws IOException {

        // Given a metadata object with example metadata.
        String expectedTitle = "expected title";
        datasetMetadata.setTitle(expectedTitle);

        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = ",Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

        // Then the expected metadata is output at the top of the XLSX sheet
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Dataset Title");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo(expectedTitle);
    }

    @Test
    public void format_WithEmptyObservation() throws IOException {

        // Given some v4 file data with an empty observation field
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = ",Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, style, style, style);

        // Then the empty observation value is in the output
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        Cell cell = sheet.getRow(metadataRows + 1).getCell(1);
        assertThat(cell.getStringCellValue()).isEqualTo("");
    }

    @Test
    public void format_ZeroDecimal() throws IOException {

        // Given some v4 file data with an observation that has a zero decimal place (88.0)
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "88.0,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, numberStyle, style, style);

        // Then the value in the output has the decimal place
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);

        Cell cell = sheet.getRow(metadataRows + 1).getCell(1);

        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(88.0);
    }

    @Test
    public void format_NoDecimal() throws IOException {

        // Given some v4 file data with an observation that has a zero decimal place (88.0)
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "88,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        // When format is called
        datasetFormatter.format(sheet, file, datasetMetadata, style, style, numberStyle, style, style);

        // Then the value in the output has the decimal place
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        Cell cell = sheet.getRow(metadataRows + 1).getCell(1);

        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(88.0);
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

    private CellStyle createNumberStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        style.setDataFormat(wb.createDataFormat().getFormat("0.0############################"));
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }
}
