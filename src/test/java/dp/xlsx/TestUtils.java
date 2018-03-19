package dp.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

// Functions to assist when testing / debugging XSLX files.
public class TestUtils {

    // Debug print the given sheet to std.out
    static void printSheet(Sheet sheet) {
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

    static void writeToFile(Workbook workbook) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        workbook.write(os);
        Files.write(Paths.get("test0.xlsx"), os.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
