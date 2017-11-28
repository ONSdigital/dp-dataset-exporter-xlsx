package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

class MetadataFormatter {

    void format(Sheet sheet, Metadata datasetMetadata, CellStyle style) {

        int columnOffset = 0;
        int rowOffset = 0;

        Row row = sheet.createRow(rowOffset);

        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(style);
        cell.setCellValue("Dataset Title");

        cell = row.createCell(columnOffset + 1);
        cell.setCellStyle(style);
        cell.setCellValue(datasetMetadata.getTitle());
        rowOffset++;

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

    }

}
