package dp.xlsx;

import dp.api.dataset.models.Metadata;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static dp.logging.LogEvent.info;

/**
 * A spring component used to convert a V4 file to a XLSX file
 */
@Component
public class Converter {

    /**
     * The maximum number of rows to hold in memory
     **/
    private static final int MAX_IN_MEMORY_ROWS = 50;

    /**
     * Convert a V4 file to a XLSX file
     *
     * @param stream A V4 file to convert
     * @return The converted V4 as XLSX file
     * @throws IOException Failed to convert the V4 file to XLSX
     */
    public Workbook toXLSX(final InputStream stream, Metadata datasetMetadata) throws IOException {
        info().log("beginning xlsx file generation");
        final Workbook workbook = new CMDWorkbook(MAX_IN_MEMORY_ROWS);
        final CellStyle headingStyle = createBoldStyle(workbook);
        final CellStyle valueStyle = createStyle(workbook);
        final CellStyle linkStyle = createLinkStyle(workbook);

        info().log("creating local copy of data from stream");
        final V4File v4File = new V4File(stream, datasetMetadata);

        info().log("creating data sheet");
        final Sheet datasetSheet = workbook.createSheet("Dataset");
        final WorkBookStyles workBookStyles = new WorkBookStyles(workbook);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(
                workBookStyles,
                datasetSheet,
                v4File,
                datasetMetadata);

        info().log("formatting data sheet");
        datasetFormatter.format();

        info().log("creating metadata sheet");
        final Sheet metadataSheet = workbook.createSheet("Metadata");
        info().log("adding metadata sheet to workbook");
        final MetadataFormatter metadataFormatter = new MetadataFormatter(
                metadataSheet,
                datasetMetadata,
                headingStyle,
                valueStyle,
                linkStyle);

        info().log("formatting metadata");
        metadataFormatter.format();
        info().log("formatting metadata completed");

        return workbook;
    }

    private CellStyle createStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createLinkStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Link");
        font.setFontHeightInPoints((short) 14);
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.TOP);

        return style;
    }

    private CellStyle createBoldStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Bold");
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(false);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

}
