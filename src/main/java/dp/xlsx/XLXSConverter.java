package dp.xlsx;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A spring componet used to convert a V4 file to a XLSX file
 */
@Component
public class XLXSConverter {


    private final Formatter formatter = new Formatter();
    /**
     * Convert a V4 file to a XLSX file
     *
     * @param stream A V4 file to convert
     * @return The converted V4 as XLSX file
     * @throws IOException Failed to convert the V4 file to XLSX
     */
    public ByteArrayOutputStream toXLXS(final InputStream stream) throws IOException {
        final Workbook wb = new XSSFWorkbook();
        final CellStyle style = createStyle(wb);
        final Sheet sheet = wb.createSheet("Dataset");

        final V4File v4File = new V4File(stream);

        formatter.format(sheet, v4File, style);

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        os.flush();
        return os;
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

