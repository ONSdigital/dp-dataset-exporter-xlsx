package dp.xlsx;

import dp.api.dataset.models.Metadata;
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
import java.util.stream.Collectors;

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
     * @param valueStyle  The style of the cell titles
     * @param numberStyle The style of the observations
     */
    void format(Sheet sheet, V4File file, Metadata datasetMetadata, CellStyle headingStyle, CellStyle headingRightAlignStyle, CellStyle valueStyle, CellStyle valueRightAlign, CellStyle numberStyle) {

        final List<Group> groups = file.groupData();
        Map<Integer, Integer> sortedPositionMapping = file.getSortedPositionMapping();

        final List<SortedGroup> sortedGroups = groups
                .stream()
                .map(group -> new SortedGroup(group, sortedPositionMapping))
                .sorted()
                .collect(Collectors.toList());


        final Collection<String> timeLabels = file.getOrderedTimeLabels();

        final Map<String, Row> timeRows = new HashMap<>();
        int columnOffset = 0;
        int rowOffset = 0;

        rowOffset = addMetadataRows(sheet, rowOffset);

        // Start off by placing the time on all rows
        int i = 0;
        for (String timeLabel : timeLabels) {
            Row row = sheet.createRow(i + rowOffset + 1);
            Cell cell = row.createCell(columnOffset);
            cell.setCellStyle(valueStyle);
            cell.setCellValue(timeLabel);
            timeRows.put(timeLabel, row);
            i++;
        }

        Row title = sheet.createRow(rowOffset);

        // add dimension names in the corner of the table
        Cell cell = title.createCell(columnOffset);
        cell.setCellStyle(valueRightAlign);

        if (datasetMetadata.getDimensions() != null) {
            String dimensionNames = file.getDimensions()
                    .stream()
                    .map(d -> StringUtils.capitalize(d))
                    .sorted()
                    .collect(Collectors.joining("\n"));

            cell.setCellValue(dimensionNames);
        }

        columnOffset += 1;

        // For each group add the title onto the row
        for (int g = 0; g < sortedGroups.size(); g++) {

            cell = title.createCell(g + columnOffset);
            cell.setCellStyle(valueStyle);
            SortedGroup group = sortedGroups.get(g);
            cell.setCellValue(group.getTitle());
            sheet.autoSizeColumn(g + columnOffset);

            // For each time label add the observation into the correct row
            for (String timeTitle : timeLabels) {
                Row row = timeRows.get(timeTitle);
                Cell obs = row.createCell(g + columnOffset);

                final String value = group.getObservation(timeTitle);

                if (StringUtils.isEmpty(value)) {
                    obs.setCellValue("");
                    continue;
                }

                if (value.contains(".")) {
                    obs.setCellStyle(numberStyle); // apply decimal formatting if there is a decimal
                } else {
                    obs.setCellStyle(valueStyle);
                }

                try {
                    obs.setCellValue(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    obs.setCellValue("");
                }
            }
        }

        // populate the metadata after the columns have been autosized around the dimension headers
        addMetadata(sheet, datasetMetadata, headingStyle, headingRightAlignStyle, 0, 0);

        sheet.autoSizeColumn(0);
    }


    private int addMetadataRows(Sheet sheet, int rowOffset) {

        // title row
        sheet.createRow(rowOffset);
        rowOffset++;

        // Add a blank row at the bottom of the metadata.
        sheet.createRow(rowOffset);
        rowOffset++;
        return rowOffset;
    }

    private void addMetadata(Sheet sheet, Metadata datasetMetadata, CellStyle headingStyle, CellStyle headingRightAlignStyle, int columnOffset, int rowOffset) {

        Row row = sheet.getRow(rowOffset);

        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(headingRightAlignStyle);
        cell.setCellValue("Dataset Title");

        cell = row.createCell(columnOffset + 1);
        cell.setCellStyle(headingStyle);
        cell.setCellValue(datasetMetadata.getTitle());
    }

}
