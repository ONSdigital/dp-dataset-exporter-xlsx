package dp.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


public class FormatterTest {

    private final Formatter formatter = new Formatter();

    @Test
    public void createXlsxFormat() throws IOException {

        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {

            final V4File file = new V4File(stream);
            final Workbook wb = new XSSFWorkbook();
            final CellStyle style = createStyle(wb);
            final Sheet sheet = wb.createSheet("Test");

            formatter.format(sheet, file, style, style);

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
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Test");

        // When format is called
        formatter.format(sheet, file, style, style);

        // Then the empty observation value is in the output
        assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("");
    }

    @Test
    public void timeValuesAreOrderedAlphabetically() throws IOException {

        // Given some V4 input data
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow = "45.2,Month,Jan-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "86.9,Month,Feb-96,K02000001,Great Britain,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow + csvRow2;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        final V4File file = new V4File(inputStream);
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Test");

        // When format is called
        formatter.format(sheet, file, style, style);

        printSheet(sheet);

        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("Feb-96");
        assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("Jan-96");
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
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Test");

        // When format is called
        formatter.format(sheet, file, style, style);

        printSheet(sheet);

        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("England (K02000003)\nBBB (cpi1dim1A2)");
        assertThat(sheet.getRow(0).getCell(2).getStringCellValue()).isEqualTo("Wales (K02000002)\nAAA (cpi1dim1A1)");
    }

    private void printSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {

                Cell cell = cellIterator.next();

                if (cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                    System.out.print(cell.getNumericCellValue() + ",");
                } else {
                    System.out.print(cell.getStringCellValue().replace("\n", ":") + ",");
                }
            }

            System.out.println();
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
