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

    private final Metadata datasetMetadata;
    private final CellStyle titleStyle;
    private final CellStyle valueStyle;
    private final Sheet sheet;

    private int rowOffset;

    public MetadataFormatter(Sheet sheet, Metadata datasetMetadata, CellStyle titleStyle, CellStyle valueStyle) {
        this.datasetMetadata = datasetMetadata;
        this.titleStyle = titleStyle;
        this.valueStyle = valueStyle;
        this.sheet = sheet;
    }

    void format() {

        rowOffset = 0;

        writeIndividualValues();
        writeContactDetails();
        writeKeywords();
        writeAlerts();
        writeCodeLists();
        writeDistributions();
        writeDownloads();
        writeLatestChanges();
        writeLinks();
        writeMethodologies();
        writePublications();
        writeQMI();
        writeRelatedDatasets();
        writeTemporalFrequencies();

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeTemporalFrequencies() {

        for (TemporalFrequency frequency : datasetMetadata.getTemporal()) {
            writeBlankRow();
            writeString("Temporal Frequency", frequency.getFrequency());
            writeString("Temporal Start Date", frequency.getStartDate());
            writeString("Temporal End Date", frequency.getEndDate());
        }
    }

    private int writeRelatedDatasets() {

        for (GeneralDetails details : datasetMetadata.getRelatedDatasets()) {
            writeBlankRow();
            writeString("Related dataset Title", details.getTitle());
            writeString("Related dataset Description", details.getDescription());
            writeString("Related dataset URL", details.getHref());
        }

        return rowOffset;
    }

    private int writeQMI() {

        writeBlankRow();
        writeString("QMI Title", datasetMetadata.getQmi().getTitle());
        writeString("QMI Description", datasetMetadata.getQmi().getDescription());
        writeString("QMI URL", datasetMetadata.getQmi().getHref());

        return rowOffset;
    }

    private int writePublications() {

        for (GeneralDetails details : datasetMetadata.getPublications()) {
            writeBlankRow();
            writeString("Publication Title", details.getTitle());
            writeString("Publication Description", details.getDescription());
            writeString("Publication URL", details.getHref());
        }

        return rowOffset;
    }

    private int writeMethodologies() {

        for (GeneralDetails details : datasetMetadata.getMethodologies()) {
            writeBlankRow();
            writeString("Methodology Title", details.getTitle());
            writeString("Methodology Description", details.getDescription());
            writeString("Methodology URL", details.getHref());
        }

        return rowOffset;
    }

    private int writeLinks() {

        writeBlankRow();
        writeString("Access Rights URL", datasetMetadata.getLinks().getAccessRights().getHref());
        writeString("Spatial URL", datasetMetadata.getLinks().getSpatial().getHref());
        writeString("Dataset Version URL", datasetMetadata.getLinks().getVersion().getHref());

        return rowOffset;
    }

    private int writeLatestChanges() {

        for (LatestChange change : datasetMetadata.getLatestChanges()) {

            writeBlankRow();
            writeString("Change Name", change.getName());
            writeString("Change Type", change.getType());
            writeString("Change Description", change.getDescription());
        }

        return rowOffset;
    }

    private int writeDownloads() {

        writeBlankRow();
        writeString("XLSX URL", datasetMetadata.getDownloads().getXls().getUrl());
        writeString("XLSX File Size (bytes)", datasetMetadata.getDownloads().getXls().getSize());

        writeBlankRow();
        writeString("CSV URL", datasetMetadata.getDownloads().getCsv().getUrl());
        writeString("CSV File Size (bytes)", datasetMetadata.getDownloads().getCsv().getSize());

        return rowOffset;
    }

    private int writeDistributions() {

        writeBlankRow();
        for (String distribution : datasetMetadata.getDistribution()) {
            writeString("Distribution", distribution);
        }

        return rowOffset;
    }

    private int writeCodeLists() {

        for (CodeList codelist : datasetMetadata.getDimensions()) {
            writeBlankRow();
            writeString("Code List Name", codelist.getName());
            writeString("Code List Description", codelist.getDescription());
            writeString("Code List ID", codelist.getId());
            writeString("Code List URL", codelist.getHref());
        }

        return rowOffset;
    }

    private int writeAlerts() {

        for (Alert alert : datasetMetadata.getAlerts()) {

            writeBlankRow();
            writeString("Alert Date", alert.getDate());
            writeString("Alert Type", alert.getType());
            writeString("Alert Description", alert.getDescription());
        }

        return rowOffset;
    }

    private int writeKeywords() {

        writeBlankRow();
        for (String keyword : datasetMetadata.getKeywords()) {
            writeString("Keyword", keyword);
        }

        return rowOffset;
    }

    private int writeContactDetails() {

        for (ContactDetails contact : datasetMetadata.getContacts()) {

            writeBlankRow();
            writeString("Contact Name", contact.getName());
            writeString("Contact Telephone", contact.getTelephone());
            writeString("Contact Email", contact.getEmail());
        }

        return rowOffset;
    }

    private int writeIndividualValues() {

        writeString("Dataset Title", datasetMetadata.getTitle());
        writeString("Description", datasetMetadata.getDescription());
        writeString("Release Date", datasetMetadata.getReleaseDate());
        writeString("Dataset URL", datasetMetadata.getUri());
        writeString("Licence", datasetMetadata.getLicense());
        writeString("Theme", datasetMetadata.getTheme());
        writeString("Unit of Measure", datasetMetadata.getUnitOfMeasure());
        writeBoolean("National Statistic", datasetMetadata.getNationalStatistic());
        writeString("Next Release", datasetMetadata.getNextRelease());
        writeString("Release Frequency", datasetMetadata.getReleaseFrequency());

        writeBlankRow();
        writeString("Publisher Name", datasetMetadata.getPublisher().getName());
        writeString("Publisher Type", datasetMetadata.getPublisher().getType());
        writeString("Publisher URL", datasetMetadata.getPublisher().getHref());

        return rowOffset;
    }

    private void writeBlankRow() {
        sheet.createRow(rowOffset);
        rowOffset++;
    }

    private void writeBoolean(String title, Boolean boolValue) {

        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(valueStyle);

        String value = boolValue ? "yes" : "no";
        cell.setCellValue(value);
        rowOffset++;
    }

    private void writeString(String title, String value) {

        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(valueStyle);
        cell.setCellValue(value);
        rowOffset++;
    }
}
