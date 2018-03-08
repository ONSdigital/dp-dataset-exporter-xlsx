package dp.xlsx;

import dp.api.dataset.models.Metadata;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * A spring component used to convert a V4 file to a XLSX file
 */
@Component
public class Converter {

    private final static Logger LOGGER = LoggerFactory.getLogger(Converter.class);

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
        LOGGER.info("beginning xlsx file generation");
        final Workbook workbook = new CMDWorkbook(MAX_IN_MEMORY_ROWS);
        final CellStyle headingStyle = createBoldStyle(workbook);
        final CellStyle headerRightAlignStyle = createBoldRightAlignStyle(workbook);
        final CellStyle valueStyle = createStyle(workbook);
        final CellStyle valueRightAlignStyle = createRightAlignStyle(workbook);
        final CellStyle linkStyle = createLinkStyle(workbook);
        final CellStyle numberStyle = createNumberStyle(workbook);
        final CellStyle noteStyle = createNoteStyle(workbook);

        LOGGER.info("creating local copy of data from stream");
        final V4File v4File = new V4File(stream);

        LOGGER.info("creating data sheet");
        final Sheet datasetSheet = workbook.createSheet("Dataset");
        final WorkBookStyles workBookStyles = new WorkBookStyles(workbook);
        final DatasetFormatter datasetFormatter = new DatasetFormatter(
                workBookStyles,
                datasetSheet,
                v4File,
                datasetMetadata);

        LOGGER.info("formatting data sheet");
        datasetFormatter.format();

        LOGGER.info("creating metadata sheet");
        final Sheet metadataSheet = workbook.createSheet("Metadata");
        LOGGER.info("adding metadata sheet to workbook");
        final MetadataFormatter metadataFormatter = new MetadataFormatter(
                metadataSheet,
                datasetMetadata,
                headingStyle,
                valueStyle,
                linkStyle);

        LOGGER.info("formatting metadata");
        metadataFormatter.format();
        LOGGER.info("formatting metadata completed");

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

    private CellStyle createRightAlignStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.RIGHT);
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

    private CellStyle createBoldRightAlignStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Bold");
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createNumberStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Number");
        font.setFontHeightInPoints((short) 14);
        style.setDataFormat(wb.createDataFormat().getFormat("0.0############################"));
        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createNoteStyle(Workbook wb) {
        CellStyle style = createStyle(wb);
        style.setWrapText(false);
        return style;
    }

}
