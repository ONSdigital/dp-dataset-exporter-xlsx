package dp.xlsx;

import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.UsageNotes;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class used to format a V4 file into a two dimensional structure for a xlsx
 * file.
 */
class DatasetFormatter {

	private final int COLUMN_WIDTH_PADDING_CHARS = 3;
	private final int DIMENSION_WIDTH_PADDING_CHARS = 5;
	private final int EXCEL_CHARS_TO_WIDTH_FACTOR = 275;

	private final WorkBookStyles workBookStyles;

	private final Sheet sheet;
	private final V4File file;
	private final Metadata datasetMetadata;

	// Maintain a map of column index to width. As we write rows see if the width
	// needs to be larger.
	private final Map<Integer, Integer> columnWidths = new HashMap<>();

	private int rowOffset = 0;

	public DatasetFormatter(WorkBookStyles workBookStyles, Sheet sheet, V4File file, Metadata datasetMetadata) {

		if (file.getDimensions() == null) {
			throw new IllegalArgumentException("dimensions in the dataset cannot be null");
		}

		this.workBookStyles = workBookStyles;
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

		// start with the column width of the first time header, then later check if any
		// observations are wider.
		int widestDataColumn = timeLabels.iterator().next().length();

		for (Group group : sortedGroups) {

			int columnOffset = 0;
			Row row = sheet.createRow(rowOffset);

			columnOffset = addDimensionOptionCells(group, columnOffset, row);
			widestDataColumn = addObservationCells(timeLabels, widestDataColumn, group, columnOffset, row);

			rowOffset++;
		}

		sheet.setDefaultColumnWidth(widestDataColumn + COLUMN_WIDTH_PADDING_CHARS);

		for (Map.Entry<Integer, Integer> columnWidth : columnWidths.entrySet()) {
			sheet.setColumnWidth(columnWidth.getKey(),
					(columnWidth.getValue() + DIMENSION_WIDTH_PADDING_CHARS) * EXCEL_CHARS_TO_WIDTH_FACTOR);
		}

		if (datasetMetadata.getUsageNotes() != null) {
			for (UsageNotes note : datasetMetadata.getUsageNotes()) {
				addUserNotes(note);
			}
		}
	}

	private int addObservationCells(Collection<String> timeLabels, int widestDataColumn, Group group, int columnOffset,
			Row row) {

		for (String timeTitle : timeLabels) {

			Cell obs = row.createCell(columnOffset);
			final Observation observation = group.getObservation(timeTitle);

			if (observation == null) {
				columnOffset += this.file.getAdditionalHeaders().length + 1;
				continue;
			}

			final String observationValue = observation.getValue();
			setObservationCellValue(obs, observationValue);
			columnOffset++;

			for (String additionalValue : observation.getAdditionalValues()) {
				Cell data = row.createCell(columnOffset);
				data.setCellValue(additionalValue);
				setCellValueByType(data, additionalValue);
				columnOffset++;
			}

			if (observationValue != null && observationValue.length() > widestDataColumn)
				widestDataColumn = observationValue.length();
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
		cell.setCellStyle(workBookStyles.getValueStyle());
		cell.setCellValue(header);

		final Integer geoCodeColumnWidth = columnWidths.get(columnOffset);
		if (geoCodeColumnWidth == null || header.length() > geoCodeColumnWidth)
			columnWidths.put(columnOffset, header.length());

	}

	private void addHeaderRow(Collection<String> timeLabels) {

		Row headerRow = sheet.createRow(rowOffset);

		int columnOffset = 0;

		for (DimensionData dimensionData : file.getDimensions()) {

			String dimensionName = StringUtils.capitalize(dimensionData.getValue());

			if (datasetMetadata.getDimensions() != null) {
				for (CodeList codeList : datasetMetadata.getDimensions()) {
					String datasetDimensionName = StringUtils.capitalize(codeList.getName());
					if (datasetDimensionName.equals(dimensionName) && !codeList.getLabel().isEmpty()) {
						dimensionName = StringUtils.capitalize(codeList.getLabel());
					}
				}
			}

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

		final String[] additionalHeaders = file.getAdditionalHeaders();

		// write time labels across the title row
		for (String timeLabel : timeLabels) {
			Cell cell = headerRow.createCell(columnOffset);
			cell.setCellStyle(workBookStyles.getValueRightAlignStyle());
			cell.setCellValue(timeLabel);
			columnOffset++;

			for (String additionalHeader : additionalHeaders) {
				addHeaderCell(columnOffset, headerRow, additionalHeader + " (" + timeLabel + ")");
				columnOffset++;
			}
		}
	}

	private void setCellValueByType(Cell cell, String value) {

		if (value.chars().allMatch(Character::isDigit)) {
			cell.setCellStyle(workBookStyles.getNumberStyle());
		} else {
			cell.setCellStyle(workBookStyles.getValueStyle());
		}
	}

	private void setObservationCellValue(Cell obs, String value) {

		if (StringUtils.isEmpty(value)) {
			obs.setCellValue("");
			return;
		}

		if (value.contains(".")) {
			obs.setCellStyle(workBookStyles.getNumberStyle()); // apply decimal formatting if there is a decimal
		} else {
			obs.setCellStyle(workBookStyles.getValueStyle());
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
		cell.setCellStyle(workBookStyles.getHeaderRightAlignStyle());
		final String titleLabel = "Title";
		cell.setCellValue(titleLabel);

		columnWidths.put(columnOffset, titleLabel.length());

		cell = row.createCell(columnOffset + 1);
		cell.setCellStyle(workBookStyles.getHeadingStyle());
		cell.setCellValue(datasetMetadata.getTitle());
		rowOffset++;

		// Add a blank row at the bottom of the metadata.
		sheet.createRow(rowOffset);
		rowOffset++;
	}

	private void addNote(final String value) {
		rowOffset++;
		Row row = sheet.createRow(rowOffset);
		row.createCell(0); // Blank cell
		Cell cell = row.createCell(1);
		cell.setCellValue(value);
		cell.setCellStyle(workBookStyles.getNoteStyle());
	}

	private void addUserNotes(UsageNotes notes) {
		rowOffset++;
		sheet.createRow(rowOffset); // Blank row
		rowOffset++;
		Row row = sheet.createRow(rowOffset);
		Cell title = row.createCell(0);
		title.setCellStyle(workBookStyles.getHeadingStyle());
		title.setCellValue(notes.getTitle());
		addNote(notes.getNotes());
	}
}
