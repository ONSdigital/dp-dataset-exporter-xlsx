package dp.xlsx;

import dp.api.dataset.Metadata;
import dp.api.dataset.MetadataTest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


public class MetadataFormatterTest {

    private final MetadataFormatter metadataFormatter = new MetadataFormatter();

    @Test
    public void TestFormat_OutputsMetadata() throws IOException {

        // Given a metadata object with example metadata.
        final Metadata metadata = MetadataTest.createTestMetadata();


        final Workbook wb = new XSSFWorkbook();
        final Sheet sheet = wb.createSheet("Test");
        final CellStyle cellStyle = wb.createCellStyle();

        // When format is called
        metadataFormatter.format(sheet, metadata, cellStyle, cellStyle);

        printSheet(sheet);

        // Then the expected metadata is output in the XLSX sheet
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Dataset Title");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo(metadata.getTitle());
    }


    private void printSheet(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                System.out.println("Row " + row.getRowNum());
                Cell cell = cellIterator.next();
                System.out.println("Column " + cell.getColumnIndex());
                System.out.println(cell.getStringCellValue() + ",");
            }
        }
    }
}
