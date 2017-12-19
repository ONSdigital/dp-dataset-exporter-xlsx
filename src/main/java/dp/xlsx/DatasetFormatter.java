package dp.xlsx;

import dp.api.dataset.Metadata;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class used to format a V4 file into a two dimensional structure for a
 * xlsx file.
 */
class DatasetFormatter {


    private final int COLUMN_WIDTH_PADDING_CHARS = 3;

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

        sheet.setDefaultColumnWidth(timeLabels.iterator().next().length() + COLUMN_WIDTH_PADDING_CHARS);

        int rowOffset = 0;

        // Maintain a map of column index to width. As we write rows see if the width needs to be larger.
        Map<Integer, Integer> columnWidths = new HashMap<>();

        rowOffset = addMetadata(sheet, datasetMetadata, headingStyle, headingRightAlignStyle, rowOffset);


        int columnOffset = 0;

        Row headerRow = sheet.createRow(rowOffset);

        if (datasetMetadata.getDimensions() != null) {

            final List<String> dimensionNames = file.getDimensions()
                    .stream()
                    .map(d -> StringUtils.capitalize(d))
                    .sorted()
                    .collect(Collectors.toList());

            for (String dimensionName : dimensionNames) {

                Cell cell = headerRow.createCell(columnOffset);
                cell.setCellStyle(valueStyle);
                cell.setCellValue(dimensionName);
                columnWidths.put(columnOffset, dimensionName.length());
                columnOffset++;
            }
        }

        // write time labels across the title row
        for (String timeLabel : timeLabels) {
            Cell cell = headerRow.createCell(columnOffset);
            cell.setCellStyle(valueStyle);
            cell.setCellValue(timeLabel);
            columnOffset++;
        }

        rowOffset++;


        for (SortedGroup group : sortedGroups) {

            columnOffset = 0;

            Row row = sheet.createRow(rowOffset);

            for (String dimensionOptionName : group.getGroupValues()) {

                Cell cell = row.createCell(columnOffset);
                cell.setCellStyle(valueStyle);
                cell.setCellValue(dimensionOptionName);

                if (dimensionOptionName.length() > columnWidths.get(columnOffset))
                    columnWidths.put(columnOffset, dimensionOptionName.length());

                columnOffset++;
            }

            for (String timeTitle : timeLabels) {

                Cell obs = row.createCell(columnOffset);
                setObservationCellValue(valueStyle, numberStyle, group, timeTitle, obs);
                columnOffset++;
            }

            rowOffset++;
        }

        for (Map.Entry<Integer, Integer> columnWidth : columnWidths.entrySet()) {
            sheet.setColumnWidth(columnWidth.getKey(), (columnWidth.getValue() + 4) * 256);
        }

    }

    private void setObservationCellValue(CellStyle valueStyle, CellStyle numberStyle, SortedGroup group, String timeTitle, Cell obs) {
        final String value = group.getObservation(timeTitle);

        if (StringUtils.isEmpty(value)) {
            obs.setCellValue("");
            return;
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


    private int addMetadata(Sheet sheet, Metadata datasetMetadata, CellStyle headingStyle, CellStyle headingRightAlignStyle, int rowOffset) {

        int columnOffset = 0;

        // title row
        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(headingRightAlignStyle);
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
