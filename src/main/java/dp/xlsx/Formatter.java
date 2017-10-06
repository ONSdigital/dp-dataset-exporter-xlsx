package dp.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to format a V4 file into a two dimensional structure for a
 *  xlsx file.
 */
class Formatter {

    /**
     * The following function will format a V4 file into a grouped
     * 2 dimensional structure.
     * eg;
     *         K02000001   |  K02000001
     *         Food        |  Clothing
     *         ------------|-----------
     * jan-97  |  99       | 77
     * feb-97  |  88       | 55
     *
     * @param sheet The exel sheet to add the data to
     * @param file The v4 file containing the data
     * @param style The style of the cells
     */
    void format(Sheet sheet, V4File file, CellStyle style) {
        List<Group> groups = file.groupData();
        int columnOffset = 0;
        int rowOffset = 1;
        List<String> timeLabels = file.getUniqueTimeLabels();
        Map<String, Row> timeRows = new HashMap<>();
        // Start off by placing the time on all rows
        for (int i=0; i < timeLabels.size(); i++) {
            Row row = sheet.createRow(i + rowOffset);
            Cell cell = row.createCell(columnOffset);
            cell.setCellStyle(style);
            cell.setCellValue(timeLabels.get(i));
            timeRows.put(timeLabels.get(i), row);
        }
        columnOffset += 1;
        Row title = sheet.createRow(0 );
        // For each group add the title onto the row
        for (int g = 0; g < groups.size(); g++) {
            Cell cell = title.createCell(g + columnOffset);
            cell.setCellStyle(style);
            cell.setCellValue(groups.get(g).getTitle());
            sheet.autoSizeColumn(g + columnOffset);
            // For each time label add the observation into the correct row
            for (String timeTitle : timeLabels) {
                Row row = timeRows.get(timeTitle);
                Cell obs = row.createCell(g + columnOffset);
                obs.setCellStyle(style);
                obs.setCellValue(groups.get(g).getObservation(timeTitle));
            }
        }

    }
}
