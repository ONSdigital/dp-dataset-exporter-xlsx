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
     * @param sheet       The excel sheet to add the data to
     * @param file        The v4 file containing the data
     * @param valueStyle  The style of the cell titles
     * @param numberStyle The style of the observations
     */
    void format(Sheet sheet, V4File file, Metadata datasetMetadata, CellStyle headingStyle, CellStyle headingRightAlignStyle, CellStyle valueStyle, CellStyle valueRightAlign, CellStyle numberStyle) {

        if (file.getDimensions() == null) {
            throw new IllegalArgumentException("dimensions in dataset cannot be null");
        }

        final List<Group> groups = file.groupData();
        Map<Integer, Integer> sortedPositionMapping = file.getSortedPositionMapping();

        final List<SortedGroup> sortedGroups = groups.stream()
                .map(group -> new SortedGroup(group, sortedPositionMapping))
                .sorted()
                .collect(Collectors.toList());

        final Collection<String> timeLabels = file.getOrderedTimeLabels();

        // start with the column width of the first time header, then later check if any observations are longer.
        int widestDataColumn = timeLabels.iterator().next().length();
        int rowOffset = 0;

        // Maintain a map of column index to width. As we write rows see if the width needs to be larger.
        Map<Integer, Integer> dimensionColumnWidths = new HashMap<>();

        rowOffset = addMetadata(sheet, datasetMetadata, headingStyle, headingRightAlignStyle, rowOffset, dimensionColumnWidths);
        rowOffset = createHeaderRow(sheet, file, valueStyle, valueRightAlign, timeLabels, rowOffset, dimensionColumnWidths);

        for (SortedGroup group : sortedGroups) {

            int columnOffset = 0;

            Row row = sheet.createRow(rowOffset);

            for (DimensionData dimension : group.getGroupValues()) {

                Cell cell = row.createCell(columnOffset);
                cell.setCellStyle(valueStyle);
                cell.setCellValue(dimension.getValue());

                final Integer width = dimensionColumnWidths.get(columnOffset);
                if (width == null || dimension.getValue().length() > width)
                    dimensionColumnWidths.put(columnOffset, dimension.getValue().length());

                columnOffset++;

                // For geography create another column / cell for the geographic code.
                if (dimension.getDimensionType().equals(DimensionType.GEOGRAPHY)) {

                    cell = row.createCell(columnOffset);
                    cell.setCellStyle(valueStyle);
                    cell.setCellValue(dimension.getCode());

                    final Integer geoCodeColumnWidth = dimensionColumnWidths.get(columnOffset);
                    if (geoCodeColumnWidth == null || dimension.getCode().length() > geoCodeColumnWidth)
                        dimensionColumnWidths.put(columnOffset, dimension.getCode().length());

                    columnOffset++;
                }
            }

            for (String timeTitle : timeLabels) {

                Cell obs = row.createCell(columnOffset);
                final String value = group.getObservation(timeTitle);
                setObservationCellValue(valueStyle, numberStyle, obs, value);

                if (value != null && value.length() > widestDataColumn)
                    widestDataColumn = value.length();

                columnOffset++;
            }

            rowOffset++;
        }

        sheet.setDefaultColumnWidth(widestDataColumn + COLUMN_WIDTH_PADDING_CHARS);

        for (Map.Entry<Integer, Integer> columnWidth : dimensionColumnWidths.entrySet()) {
            sheet.setColumnWidth(columnWidth.getKey(), (columnWidth.getValue() + 5) * 256);
        }
    }

    private int createHeaderRow(Sheet sheet, V4File file, CellStyle valueStyle, CellStyle valueRightAlign, Collection<String> timeLabels, int rowOffset, Map<Integer, Integer> dimensionColumnWidths) {

        Row headerRow = sheet.createRow(rowOffset);

        final List<DimensionData> dimensions = file.getDimensions()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        populateHeaderRow(valueStyle, valueRightAlign, timeLabels, dimensionColumnWidths, headerRow, dimensions);
        rowOffset++;

        return rowOffset;
    }

    private void populateHeaderRow(CellStyle valueStyle, CellStyle valueRightAlign, Collection<String> timeLabels, Map<Integer, Integer> dimensionColumnWidths, Row headerRow, List<DimensionData> dimensions) {

        int columnOffset = 0;

        for (DimensionData dimensionData : dimensions) {

            String dimensionName = StringUtils.capitalize(dimensionData.getValue());

            Cell cell = headerRow.createCell(columnOffset);
            cell.setCellStyle(valueStyle);
            cell.setCellValue(dimensionName);

            Integer width = dimensionColumnWidths.get(columnOffset);
            if (width == null || dimensionName.length() > width)
                dimensionColumnWidths.put(columnOffset, dimensionName.length());

            columnOffset++;

            // For geography create another column / cell for the geographic code.
            if (dimensionData.getDimensionType().equals(DimensionType.GEOGRAPHY)) {

                String header = dimensionName + " code";
                cell = headerRow.createCell(columnOffset);
                cell.setCellStyle(valueStyle);
                cell.setCellValue(header);

                width = dimensionColumnWidths.get(columnOffset);
                if (width == null || header.length() > width)
                    dimensionColumnWidths.put(columnOffset, header.length());

                columnOffset++;
            }
        }

        // write time labels across the title row
        for (String timeLabel : timeLabels) {
            Cell cell = headerRow.createCell(columnOffset);
            cell.setCellStyle(valueRightAlign);
            cell.setCellValue(timeLabel);
            columnOffset++;
        }
    }


    private void setObservationCellValue(CellStyle valueStyle, CellStyle numberStyle, Cell obs, String value) {

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

    private int addMetadata(Sheet sheet, Metadata datasetMetadata, CellStyle headingStyle, CellStyle headingRightAlignStyle, int rowOffset, Map<Integer, Integer> dimensionColumnWidths) {

        int columnOffset = 0;

        // title row
        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(headingRightAlignStyle);
        final String titleLabel = "Title";
        cell.setCellValue(titleLabel);

        dimensionColumnWidths.put(columnOffset, titleLabel.length());

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
