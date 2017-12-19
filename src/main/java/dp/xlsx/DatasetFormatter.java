package dp.xlsx;

import dp.api.dataset.models.Metadata;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class used to format a V4 file into a two dimensional structure for a
 * xlsx file.
 */
class DatasetFormatter {


    private final int COLUMN_WIDTH_PADDING_CHARS = 3;
    private final int EXCEL_CHAR_WIDTH_FACTOR = 256;

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

        rowOffset = addMetadata(sheet, datasetMetadata, headingStyle, headingRightAlignStyle, rowOffset);
        rowOffset = createHeaderRow(sheet, file, datasetMetadata, valueStyle, timeLabels, rowOffset);

        for (SortedGroup group : sortedGroups) {

            int columnOffset = 0;

            // add dimensions label
            Row row = sheet.createRow(rowOffset);
            Cell cell = row.createCell(columnOffset);
            cell.setCellStyle(valueStyle);
            cell.setCellValue(group.getTitle());
            columnOffset++;

            for (String timeTitle : timeLabels) {

                Cell obs = row.createCell(columnOffset);
                final String value = group.getObservation(timeTitle);
                setObservationCellValue(valueStyle, numberStyle, obs, value);
                columnOffset++;
            }

            rowOffset++;
        }

        final int widestGroupTitle = sortedGroups.stream()
                .mapToInt(g -> g.getTitleWidth())
                .max()
                .getAsInt();

        sheet.setColumnWidth(0, widestGroupTitle * EXCEL_CHAR_WIDTH_FACTOR);
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


    private int createHeaderRow(Sheet sheet, V4File file, Metadata datasetMetadata, CellStyle valueStyle, Collection<String> timeLabels, int rowOffset) {

        int columnOffset = 0;

        Row headerRow = sheet.createRow(rowOffset);

        // add dimension names in the corner of the table
        Cell cell = headerRow.createCell(columnOffset);
        cell.setCellStyle(valueStyle);

        if (datasetMetadata.getDimensions() != null) {
            String dimensionNames = file.getDimensions()
                    .stream()
                    .map(d -> StringUtils.capitalize(d))
                    .sorted()
                    .collect(Collectors.joining("\n"));

            cell.setCellValue(dimensionNames);
        }

        columnOffset++;

        // write time labels across the title row
        for (String timeLabel : timeLabels) {
            cell = headerRow.createCell(columnOffset);
            cell.setCellStyle(valueStyle);
            cell.setCellValue(timeLabel);
            columnOffset++;
        }

        rowOffset++;

        return rowOffset;
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
