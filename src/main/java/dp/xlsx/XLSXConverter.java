package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * A spring component used to convert a V4 file to a XLSX file
 */
@Component
public class XLSXConverter {

    private final DatasetFormatter datasetFormatter = new DatasetFormatter();

    /**
     * Convert a V4 file to a XLSX file
     *
     * @param stream A V4 file to convert
     * @return The converted V4 as XLSX file
     * @throws IOException Failed to convert the V4 file to XLSX
     */
    public Workbook toXLSX(final InputStream stream, Metadata datasetMetadata) throws IOException {

        final Workbook workbook = new XSSFWorkbook();
        final CellStyle titleStyle = createStyle(workbook);
        final CellStyle valueStyle = createNumberStyle(workbook);

        final Sheet datasetSheet = workbook.createSheet("Dataset");
        final V4File v4File = new V4File(stream);
        datasetFormatter.format(datasetSheet, v4File, datasetMetadata, titleStyle, valueStyle);

        final Sheet metadataSheet = workbook.createSheet("Metadata");
        MetadataFormatter metadataFormatter = new MetadataFormatter(metadataSheet, datasetMetadata, titleStyle, valueStyle);
        metadataFormatter.format();

        return workbook;
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
        font.setFontHeightInPoints((short) 14);
        style.setDataFormat(wb.createDataFormat().getFormat("##.###"));
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

}
