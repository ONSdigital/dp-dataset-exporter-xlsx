package dp.xlsx;

import dp.api.dataset.Alert;
import dp.api.dataset.CodeList;
import dp.api.dataset.ContactDetails;
import dp.api.dataset.GeneralDetails;
import dp.api.dataset.LatestChange;
import dp.api.dataset.Metadata;
import dp.api.dataset.TemporalFrequency;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

class MetadataFormatter {

    void format(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle) {

        int columnOffset = 0;
        int rowOffset = 0;

        rowOffset = writeIndividualValues(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeContactDetails(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeKeywords(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeAlerts(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeCodeLists(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeDistributions(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeDownloads(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeLatestChanges(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeLinks(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeMethodologies(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writePublications(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeQMI(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        rowOffset = writeRelatedDatasets(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);
        writeTemporalFrequencies(sheet, datasetMetadata, titleStyle, valueStyle, columnOffset, rowOffset);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeTemporalFrequencies(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (TemporalFrequency frequency : datasetMetadata.getTemporal()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Temporal Frequency", frequency.getFrequency());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Temporal Start Date", frequency.getStartDate());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Temporal End Date", frequency.getEndDate());
        }
    }

    private int writeRelatedDatasets(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getRelatedDatasets()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Related dataset Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Related dataset Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Related dataset URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeQMI(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "QMI Title", datasetMetadata.getQmi().getTitle());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "QMI Description", datasetMetadata.getQmi().getDescription());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "QMI URL", datasetMetadata.getQmi().getHref());
        return rowOffset;
    }

    private int writePublications(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getPublications()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publication Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publication Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publication URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeMethodologies(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getMethodologies()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Methodology Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Methodology Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Methodology URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeLinks(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Access Rights URL", datasetMetadata.getLinks().getAccessRights().getHref());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Spatial URL", datasetMetadata.getLinks().getSpatial().getHref());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Dataset Version URL", datasetMetadata.getLinks().getVersion().getHref());
        return rowOffset;
    }

    private int writeLatestChanges(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (LatestChange change : datasetMetadata.getLatestChanges()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Change Name", change.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Change Type", change.getType());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Change Description", change.getDescription());
        }
        return rowOffset;
    }

    private int writeDownloads(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "XLSX URL", datasetMetadata.getDownloads().getXls().getUrl());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "XLSX File Size (bytes)", datasetMetadata.getDownloads().getXls().getSize());

        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "CSV URL", datasetMetadata.getDownloads().getCsv().getUrl());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "CSV File Size (bytes)", datasetMetadata.getDownloads().getCsv().getSize());
        return rowOffset;
    }

    private int writeDistributions(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        for (String distribution : datasetMetadata.getDistribution()) {
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Distribution", distribution);
        }
        return rowOffset;
    }

    private int writeCodeLists(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (CodeList codelist : datasetMetadata.getDimensions()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Code List Name", codelist.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Code List Description", codelist.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Code List ID", codelist.getId());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Code List URL", codelist.getHref());
        }
        return rowOffset;
    }

    private int writeAlerts(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (Alert alert : datasetMetadata.getAlerts()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Alert Date", alert.getDate());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Alert Type", alert.getType());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Alert Description", alert.getDescription());
        }
        return rowOffset;
    }

    private int writeKeywords(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        for (String keyword : datasetMetadata.getKeywords()) {
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Keyword", keyword);
        }
        return rowOffset;
    }

    private int writeContactDetails(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        for (ContactDetails contact : datasetMetadata.getContacts()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Contact Name", contact.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Contact Telephone", contact.getTelephone());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Contact Email", contact.getEmail());
        }
        return rowOffset;
    }

    private int writeIndividualValues(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset) {
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Dataset Title", datasetMetadata.getTitle());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Description", datasetMetadata.getDescription());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Release Date", datasetMetadata.getReleaseDate());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Dataset URL", datasetMetadata.getUri());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Licence", datasetMetadata.getLicense());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Theme", datasetMetadata.getTheme());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Unit of Measure", datasetMetadata.getUnitOfMeasure());
        rowOffset = writeBooleanProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "National Statistic", datasetMetadata.getNationalStatistic());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Next Release", datasetMetadata.getNextRelease());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Release Frequency", datasetMetadata.getReleaseFrequency());

        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publisher Name", datasetMetadata.getPublisher().getName());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publisher Type", datasetMetadata.getPublisher().getType());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, columnOffset, rowOffset, "Publisher URL", datasetMetadata.getPublisher().getHref());
        return rowOffset;
    }

    private int writeBlankRow(Sheet sheet, int rowOffset) {
        sheet.createRow(rowOffset);
        rowOffset++;
        return rowOffset;
    }

    private int writeBooleanProperty(Sheet sheet, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset, String title, Boolean boolValue) {
        Row row;
        Cell cell;
        row = sheet.createRow(rowOffset);
        cell = row.createCell(columnOffset);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(columnOffset + 1);
        cell.setCellStyle(valueStyle);

        String value = boolValue ? "yes" : "no";
        cell.setCellValue(value);
        rowOffset++;
        return rowOffset;
    }

    private int writeStringProperty(Sheet sheet, CellStyle titleStyle, CellStyle valueStyle, int columnOffset, int rowOffset, String title, String value) {
        Row row;
        Cell cell;
        row = sheet.createRow(rowOffset);
        cell = row.createCell(columnOffset);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(columnOffset + 1);
        cell.setCellStyle(valueStyle);
        cell.setCellValue(value);
        rowOffset++;
        return rowOffset;
    }
}
