package dp.xlsx;

import dp.api.dataset.models.Metadata;
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
    private final int DIMENSION_WIDTH_PADDING_CHARS = 5;
    private final int EXCEL_CHARS_TO_WIDTH_FACTOR = 256;

    private final CellStyle headingStyle;
    private final CellStyle headingRightAlignStyle;
    private final CellStyle valueStyle;
    private final CellStyle valueRightAlign;
    private final CellStyle numberStyle;

    private final Sheet sheet;
    private final V4File file;
    private final Metadata datasetMetadata;

    // Maintain a map of column index to width. As we write rows see if the width needs to be larger.
    private final Map<Integer, Integer> dimensionColumnWidths = new HashMap<>();

    private int rowOffset = 0;

    public DatasetFormatter(CellStyle headingStyle, CellStyle headingRightAlignStyle, CellStyle valueStyle, CellStyle valueRightAlign, CellStyle numberStyle, Sheet sheet, V4File file, Metadata datasetMetadata) {

        if (file.getDimensions() == null) {
            throw new IllegalArgumentException("dimensions in the dataset cannot be null");
        }

        this.headingStyle = headingStyle;
        this.headingRightAlignStyle = headingRightAlignStyle;
        this.valueStyle = valueStyle;
        this.valueRightAlign = valueRightAlign;
        this.numberStyle = numberStyle;
        this.sheet = sheet;
        this.file = file;
        this.datasetMetadata = datasetMetadata;
    }

    void format() {

        final Collection<Group> groups = file.groupData();
        final Collection<String> timeLabels = file.getOrderedTimeLabels();

        List<Group> sortedGroups = groups.stream().sorted().collect(Collectors.toList());

        addMetadata();
        addHeaderRow(timeLabels);

        // start with the column width of the first time header, then later check if any observations are wider.
        int widestDataColumn = timeLabels.iterator().next().length();

        for (Group group : sortedGroups) {

            int columnOffset = 0;
            Row row = sheet.createRow(rowOffset);

            columnOffset = addDimensionOptionCells(group, columnOffset, row);
            widestDataColumn = addObservationCells(timeLabels, widestDataColumn, group, columnOffset, row);

            rowOffset++;
        }

        sheet.setDefaultColumnWidth(widestDataColumn + COLUMN_WIDTH_PADDING_CHARS);

        for (Map.Entry<Integer, Integer> columnWidth : dimensionColumnWidths.entrySet()) {
            sheet.setColumnWidth(columnWidth.getKey(),
                    (columnWidth.getValue() + DIMENSION_WIDTH_PADDING_CHARS) * EXCEL_CHARS_TO_WIDTH_FACTOR);
        }
    }

    private int addObservationCells(Collection<String> timeLabels, int widestDataColumn, Group group, int columnOffset, Row row) {

        for (String timeTitle : timeLabels) {

            Cell obs = row.createCell(columnOffset);
            final String value = group.getObservation(timeTitle);
            setObservationCellValue(obs, value);

            if (value != null && value.length() > widestDataColumn)
                widestDataColumn = value.length();

            columnOffset++;
        }

        return widestDataColumn;
    }

    private int addDimensionOptionCells(Group group, int columnOffset, Row row) {

        for (DimensionData dimension : group.getGroupValues()) {

            addHeaderCell(columnOffset, row, dimension.getValue());
            columnOffset++;

            // For geography create another column / cell for the geographic code.
            if (dimension.getDimensionType().equals(DimensionType.GEOGRAPHY)) {
                addHeaderCell(columnOffset, row, dimension.getCode());
                columnOffset++;
            }
        }

        return columnOffset;
    }

    private void addHeaderCell(int columnOffset, Row row, String header) {

        Cell cell = row.createCell(columnOffset);
        cell.setCellStyle(valueStyle);
        cell.setCellValue(header);

        final Integer geoCodeColumnWidth = dimensionColumnWidths.get(columnOffset);
        if (geoCodeColumnWidth == null || header.length() > geoCodeColumnWidth)
            dimensionColumnWidths.put(columnOffset, header.length());

    }

    private void addHeaderRow(Collection<String> timeLabels) {

        Row headerRow = sheet.createRow(rowOffset);

        int columnOffset = 0;

        for (DimensionData dimensionData : file.getDimensions()) {

            String dimensionName = StringUtils.capitalize(dimensionData.getValue());
            addHeaderCell(columnOffset, headerRow, dimensionName);
            columnOffset++;

            // For geography create another column / cell for the geographic code.
            if (dimensionData.getDimensionType().equals(DimensionType.GEOGRAPHY)) {

                String header = dimensionName + " code";
                addHeaderCell(columnOffset, headerRow, header);
                columnOffset++;
            }
        }

        addTimeLabelCells(timeLabels, headerRow, columnOffset);

        rowOffset++;
    }

    private void addTimeLabelCells(Collection<String> timeLabels, Row headerRow, int columnOffset) {

        // write time labels across the title row
        for (String timeLabel : timeLabels) {
            Cell cell = headerRow.createCell(columnOffset);
            cell.setCellStyle(valueRightAlign);
            cell.setCellValue(timeLabel);
            columnOffset++;
        }
    }


    private void setObservationCellValue(Cell obs, String value) {

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

    private void addMetadata() {

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
    }
}
