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

        int rowOffset = 0;

        rowOffset = writeIndividualValues(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeContactDetails(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeKeywords(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeAlerts(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeCodeLists(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeDistributions(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeDownloads(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeLatestChanges(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeLinks(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeMethodologies(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writePublications(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeQMI(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        rowOffset = writeRelatedDatasets(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);
        writeTemporalFrequencies(sheet, datasetMetadata, titleStyle, valueStyle, rowOffset);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeTemporalFrequencies(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (TemporalFrequency frequency : datasetMetadata.getTemporal()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Temporal Frequency", frequency.getFrequency());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Temporal Start Date", frequency.getStartDate());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Temporal End Date", frequency.getEndDate());
        }
    }

    private int writeRelatedDatasets(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getRelatedDatasets()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Related dataset Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Related dataset Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Related dataset URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeQMI(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "QMI Title", datasetMetadata.getQmi().getTitle());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "QMI Description", datasetMetadata.getQmi().getDescription());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "QMI URL", datasetMetadata.getQmi().getHref());
        return rowOffset;
    }

    private int writePublications(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getPublications()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publication Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publication Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publication URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeMethodologies(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (GeneralDetails details : datasetMetadata.getMethodologies()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Methodology Title", details.getTitle());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Methodology Description", details.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Methodology URL", details.getHref());
        }
        return rowOffset;
    }

    private int writeLinks(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Access Rights URL", datasetMetadata.getLinks().getAccessRights().getHref());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Spatial URL", datasetMetadata.getLinks().getSpatial().getHref());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Dataset Version URL", datasetMetadata.getLinks().getVersion().getHref());
        return rowOffset;
    }

    private int writeLatestChanges(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (LatestChange change : datasetMetadata.getLatestChanges()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Change Name", change.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Change Type", change.getType());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Change Description", change.getDescription());
        }
        return rowOffset;
    }

    private int writeDownloads(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "XLSX URL", datasetMetadata.getDownloads().getXls().getUrl());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "XLSX File Size (bytes)", datasetMetadata.getDownloads().getXls().getSize());

        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "CSV URL", datasetMetadata.getDownloads().getCsv().getUrl());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "CSV File Size (bytes)", datasetMetadata.getDownloads().getCsv().getSize());
        return rowOffset;
    }

    private int writeDistributions(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        for (String distribution : datasetMetadata.getDistribution()) {
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Distribution", distribution);
        }
        return rowOffset;
    }

    private int writeCodeLists(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (CodeList codelist : datasetMetadata.getDimensions()) {
            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Code List Name", codelist.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Code List Description", codelist.getDescription());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Code List ID", codelist.getId());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Code List URL", codelist.getHref());
        }
        return rowOffset;
    }

    private int writeAlerts(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (Alert alert : datasetMetadata.getAlerts()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Alert Date", alert.getDate());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Alert Type", alert.getType());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Alert Description", alert.getDescription());
        }
        return rowOffset;
    }

    private int writeKeywords(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeBlankRow(sheet, rowOffset);
        for (String keyword : datasetMetadata.getKeywords()) {
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Keyword", keyword);
        }
        return rowOffset;
    }

    private int writeContactDetails(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        for (ContactDetails contact : datasetMetadata.getContacts()) {

            rowOffset = writeBlankRow(sheet, rowOffset);
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Contact Name", contact.getName());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Contact Telephone", contact.getTelephone());
            rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Contact Email", contact.getEmail());
        }
        return rowOffset;
    }

    private int writeIndividualValues(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle, int rowOffset) {
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Dataset Title", datasetMetadata.getTitle());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Description", datasetMetadata.getDescription());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Release Date", datasetMetadata.getReleaseDate());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Dataset URL", datasetMetadata.getUri());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Licence", datasetMetadata.getLicense());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Theme", datasetMetadata.getTheme());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Unit of Measure", datasetMetadata.getUnitOfMeasure());
        rowOffset = writeBooleanProperty(sheet, titleStyle, valueStyle, rowOffset, "National Statistic", datasetMetadata.getNationalStatistic());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Next Release", datasetMetadata.getNextRelease());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Release Frequency", datasetMetadata.getReleaseFrequency());

        rowOffset = writeBlankRow(sheet, rowOffset);
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publisher Name", datasetMetadata.getPublisher().getName());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publisher Type", datasetMetadata.getPublisher().getType());
        rowOffset = writeStringProperty(sheet, titleStyle, valueStyle, rowOffset, "Publisher URL", datasetMetadata.getPublisher().getHref());
        return rowOffset;
    }

    private int writeBlankRow(Sheet sheet, int rowOffset) {
        sheet.createRow(rowOffset);
        rowOffset++;
        return rowOffset;
    }

    private int writeBooleanProperty(Sheet sheet, CellStyle titleStyle, CellStyle valueStyle, int rowOffset, String title, Boolean boolValue) {
        Row row;
        Cell cell;
        row = sheet.createRow(rowOffset);
        cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(valueStyle);

        String value = boolValue ? "yes" : "no";
        cell.setCellValue(value);
        rowOffset++;
        return rowOffset;
    }

    private int writeStringProperty(Sheet sheet, CellStyle titleStyle, CellStyle valueStyle, int rowOffset, String title, String value) {
        Row row;
        Cell cell;
        row = sheet.createRow(rowOffset);
        cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(valueStyle);
        cell.setCellValue(value);
        rowOffset++;
        return rowOffset;
    }
}
