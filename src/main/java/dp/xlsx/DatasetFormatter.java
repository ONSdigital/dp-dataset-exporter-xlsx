package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to format a V4 file into a two dimensional structure for a
 * xlsx file.
 */
class DatasetFormatter {

    /**
     * The following function will format a V4 file into a grouped
     * 2 dimensional structure.
     * eg;
     * K02000001   |  K02000001
     * Food        |  Clothing
     * ------------|-----------
     * jan-97  |  99       | 77
     * feb-97  |  88       | 55
     *
     * @param sheet       The exel sheet to add the data to
     * @param file        The v4 file containing the data
     * @param titleStyle  The style of the cell titles
     * @param numberStyle The style of the observations
     */
    void format(Sheet sheet, V4File file, Metadata datasetMetadata, CellStyle headingStyle, CellStyle titleStyle, CellStyle numberStyle) {

        final List<Group> groups = file.groupData();
        Collections.sort(groups);

        final Collection<String> timeLabels = file.getOrderedTimeLabels();

        final Map<String, Row> timeRows = new HashMap<>();
        int columnOffset = 0;
        int rowOffset = 0;

        rowOffset = addMetadata(sheet, datasetMetadata, headingStyle, columnOffset, rowOffset);

        // Start off by placing the time on all rows

        int i = 0;
        for (String timeLabel : timeLabels) {
            Row row = sheet.createRow(i + rowOffset + 1);
            Cell cell = row.createCell(columnOffset);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(timeLabel);
            timeRows.put(timeLabel, row);
            i++;
        }

        columnOffset += 1;

        Row title = sheet.createRow(rowOffset);

        // For each group add the title onto the row
        for (int g = 0; g < groups.size(); g++) {
            Cell cell = title.createCell(g + columnOffset);
            cell.setCellStyle(titleStyle);
            cell.setCellValue(groups.get(g).getTitle());
            sheet.autoSizeColumn(g + columnOffset);

            // For each time label add the observation into the correct row
            for (String timeTitle : timeLabels) {
                Row row = timeRows.get(timeTitle);
                Cell obs = row.createCell(g + columnOffset);

                final String value = groups.get(g).getObservation(timeTitle);

                if (StringUtils.isEmpty(value)) {
                    obs.setCellValue("");
                    continue;
                }

                if (value.contains(".")) {
                    obs.setCellStyle(numberStyle); // apply decimal formatting if there is a decimal
                } else {
                    obs.setCellStyle(titleStyle);
                }

                try {
                    obs.setCellValue(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    obs.setCellValue("");
                }
            }
        }

        sheet.autoSizeColumn(0);

    }

    private int addMetadata(Sheet sheet, Metadata datasetMetadata, CellStyle headingStyle, int columnOffset, int rowOffset) {

        Row row = sheet.createRow(rowOffset);

        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(headingStyle);
        cell.setCellValue("Dataset Title");

        cell = row.createCell(columnOffset + 1);
        cell.setCellStyle(headingStyle);
        cell.setCellValue(datasetMetadata.getTitle());
        rowOffset++;

        // Add a blank row at the bottom of the metadata.
        sheet.createRow(rowOffset);
        rowOffset++;
        return rowOffset;
    }

}
