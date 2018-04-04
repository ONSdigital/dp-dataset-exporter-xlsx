package dp.xlsx;

import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.UsageNotes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatasetFormatterTest {

    private static final int metadataRows = 2;
    private static final String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";

    private final Metadata datasetMetadata = new Metadata();
    private final Workbook wb = new XSSFWorkbook();
    private final Sheet sheet = wb.createSheet("Test");
    private final WorkBookStyles workBookStyles = new WorkBookStyles(wb);

    @Test
    public void timeValuesAreOrderedAlphabeticallyWhenUnrecognised() throws IOException {

        // Given some V4 input data
        String csvRow = "45.2,Month,January-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "86.9,Month,February-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);

        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        assertThat(sheet.getRow(metadataRows + 0).getCell(3).getStringCellValue()).isEqualTo("February-96");
        assertThat(sheet.getRow(metadataRows + 0).getCell(4).getStringCellValue()).isEqualTo("January-96");
    }

    @Test
    public void timeValuesAreOrderedChronologicallyWhenRecognised() throws IOException {

        // Given some V4 input data
        String csvRow = "45.2,Month,Jan-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "86.9,Month,Feb-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        assertThat(sheet.getRow(metadataRows + 0).getCell(3).getStringCellValue()).isEqualTo("Jan-96");
        assertThat(sheet.getRow(metadataRows + 0).getCell(4).getStringCellValue()).isEqualTo("Feb-96");
    }

    @Test
    public void dimensionsAreOrderedByV4FileOrder() throws IOException {

        // Given some V4 input data
        String csvRow = "45.2,Month,Jan-96,K02000002,Wales,cpi1dim1A1,AAA\n";
        String csvRow2 = "86.9,Month,Jan-96,K02000003,England,cpi1dim1A2,BBB\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the dimensions should be in the same order as the input file.
        assertThat(sheet.getRow(metadataRows + 1).getCell(0).getStringCellValue()).isEqualTo("England");
        assertThat(sheet.getRow(metadataRows + 1).getCell(1).getStringCellValue()).isEqualTo("K02000003");
        assertThat(sheet.getRow(metadataRows + 1).getCell(2).getStringCellValue()).isEqualTo("BBB");
        assertThat(sheet.getRow(metadataRows + 2).getCell(0).getStringCellValue()).isEqualTo("Wales");
        assertThat(sheet.getRow(metadataRows + 2).getCell(1).getStringCellValue()).isEqualTo("K02000002");
        assertThat(sheet.getRow(metadataRows + 2).getCell(2).getStringCellValue()).isEqualTo("AAA");
    }

    @Test
    public void format() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {

            final V4File file = new V4File(stream);
            final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

            datasetFormatter.format();
            TestUtils.printSheet(sheet);
            TestUtils.writeToFile(wb);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 7);
        }
    }

    @Test
    public void format_OutputsMetadata() throws IOException {

        // Given a metadata object with example metadata.
        String expectedTitle = "expected title";
        datasetMetadata.setTitle(expectedTitle);

        String csvRow = ",Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the expected metadata is output at the top of the XLSX sheet
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Title");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo(expectedTitle);
    }

    @Test
    public void format_WithEmptyObservation() throws IOException {

        // Given some v4 file data with an empty observation field
        String csvRow = ",Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the empty observation value is in the output
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        Cell cell = sheet.getRow(metadataRows + 1).getCell(3);
        assertThat(cell.getStringCellValue()).isEqualTo("");
    }

    @Test
    public void format_ZeroDecimal() throws IOException {

        // Given some v4 file data with an observation that has a zero decimal place (88.0)
        String csvRow = "88.0,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the value in the output has the decimal place
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);

        Cell cell = sheet.getRow(metadataRows + 1).getCell(3);

        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(88.0);
    }

    @Test
    public void format_NoDecimal() throws IOException {

        // Given some v4 file data with an observation that has a zero decimal place (88.0)
        String csvRow = "88,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the value in the output has the decimal place
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 2);
        Cell cell = sheet.getRow(metadataRows + 1).getCell(3);

        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(88.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_NullDimensions_ThrowsException() throws Exception {

        // Given a mock V4File that returns nil for its dimensions
        final V4File file = mock(V4File.class);
        when(file.getDimensions()).thenReturn(null);

        // When the DatsetFormatter constructor is called
        new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // Then the expected exception is thrown
    }

    @Test
    public void format_v4_2_File() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_2.csv")) {

            final V4File file = new V4File(stream);
            final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

            datasetFormatter.format();
            TestUtils.printSheet(sheet);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 7);
        }
    }

    @Test
    public void format_withSparsity() throws IOException {

        // Given some v4 file data sparsity (missing values in the grid)
        String csvRow1 = "88,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "85,Month,Jan-97,K02000001,,cpi1dim1A1,something else\n";
        String csvContent = csvHeader + csvRow1 + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

        // When format is called
        datasetFormatter.format();

        // Then the expected values are in the output
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 3);

        Cell cell = sheet.getRow(metadataRows + 1).getCell(3);
        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.NUMERIC);
        assertThat(cell.getNumericCellValue()).isEqualTo(88.0);

        // Then the sparse values are empty
        cell = sheet.getRow(metadataRows + 1).getCell(4);
        assertThat(cell.getCellTypeEnum()).isEqualTo(CellType.BLANK);
    }

    @Test
    public void format_WithUserNotes() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_2.csv")) {

            UsageNotes[] notes = new UsageNotes[2];
            notes[0] = new UsageNotes();
            notes[0].setTitle("Data Markings");
            notes[0].setNotes(". - value not available\n" + "x - value not reliable\n" + "p - provisional\n" + "r - revised");
            notes[1] = new UsageNotes();
            notes[1].setTitle("Coefficients of variation");
            notes[1].setNotes("CV <= 5% Estimates are considered precise\n" +
                    "CV > 5% and <= 10% Estimates are considered reasonably precise\n" +
                    "CV > 10% and <= 20% Estimates are considered acceptable\n" +
                    "CV > 20% Estimates are considered unreliable for practical purposes");
            datasetMetadata.setUsageNotes(notes);

            final V4File file = new V4File(stream);
            final DatasetFormatter datasetFormatter = new DatasetFormatter(workBookStyles, sheet, file, datasetMetadata);

            datasetFormatter.format();
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(metadataRows + 13);
        }
    }
}
