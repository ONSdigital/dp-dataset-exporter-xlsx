package dp.xlsx;

import au.com.bytecode.opencsv.CSVReader;
import dp.dataset.Dataset;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class XLXSConverter {

    public ByteArrayOutputStream toXLXS(final Dataset dataset, final InputStream stream) throws IOException {
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Dataset");
        try (final CSVReader reader = new CSVReader(new InputStreamReader(stream))) {
            createRow(sheet, style,"Dataset ID", dataset.getId(), 0);
            createRow(sheet, style,"Dataset Title", dataset.getTitle(), 1);
            createRow(sheet, style,"Publisher", "", 2);
            createRow(sheet, style,"Release Date", "", 3);
            createRow(sheet, style,"", "", 4);
            final int ROW_OFFSET = 5;
            List<String[]> data = reader.readAll();
            for (int r = 0; r < data.size(); r++){
                String[] csvRow = data.get(r);
                final Row row = sheet.createRow(r + ROW_OFFSET);
                for (int c = 0; c < csvRow.length; c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(csvRow[c]);
                    cell.setCellStyle(style);
                }
            }
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            wb.write(os);
            os.flush();
            return os;
        }
    }

    private void createRow(Sheet sheet, final CellStyle style, final String title, final String description, final int rowLocation) {
        final Row row = sheet.createRow(rowLocation);
        final Cell cellTitle = row.createCell(0);
        cellTitle.setCellValue(title);
        cellTitle.setCellStyle(style);

        final Cell cellDescription = row.createCell(1);
        cellDescription.setCellValue(description);
        cellDescription.setCellStyle(style);
    }

    private CellStyle createStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short)14);
        style.setFont(font);
        return style;
    }



}
