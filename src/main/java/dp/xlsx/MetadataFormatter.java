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
        writePublisher();
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

    private void writePublisher() {

        if (datasetMetadata.getPublisher() == null)
            return;

        writeBlankRow();
        writeString("Publisher Name", datasetMetadata.getPublisher().getName());
        writeString("Publisher Type", datasetMetadata.getPublisher().getType());
        writeString("Publisher URL", datasetMetadata.getPublisher().getHref());
    }

    private void writeTemporalFrequencies() {

        if (datasetMetadata.getTemporal() == null)
            return;

        for (TemporalFrequency frequency : datasetMetadata.getTemporal()) {
            writeBlankRow();
            writeString("Temporal Frequency", frequency.getFrequency());
            writeString("Temporal Start Date", frequency.getStartDate());
            writeString("Temporal End Date", frequency.getEndDate());
        }
    }

    private void writeRelatedDatasets() {

        if (datasetMetadata.getRelatedDatasets() == null)
            return;

        for (GeneralDetails details : datasetMetadata.getRelatedDatasets()) {
            writeBlankRow();
            writeString("Related dataset Title", details.getTitle());
            writeString("Related dataset Description", details.getDescription());
            writeString("Related dataset URL", details.getHref());
        }
    }

    private void writeQMI() {

        if (datasetMetadata.getQmi() == null)
            return;

        writeBlankRow();
        writeString("QMI Title", datasetMetadata.getQmi().getTitle());
        writeString("QMI Description", datasetMetadata.getQmi().getDescription());
        writeString("QMI URL", datasetMetadata.getQmi().getHref());
    }

    private void writePublications() {

        if (datasetMetadata.getPublications() == null)
            return;

        for (GeneralDetails details : datasetMetadata.getPublications()) {
            writeBlankRow();
            writeString("Publication Title", details.getTitle());
            writeString("Publication Description", details.getDescription());
            writeString("Publication URL", details.getHref());
        }
    }

    private void writeMethodologies() {

        if (datasetMetadata.getMethodologies() == null)
            return;

        for (GeneralDetails details : datasetMetadata.getMethodologies()) {
            writeBlankRow();
            writeString("Methodology Title", details.getTitle());
            writeString("Methodology Description", details.getDescription());
            writeString("Methodology URL", details.getHref());
        }
    }

    private void writeLinks() {

        if (datasetMetadata.getLinks() == null)
            return;

        writeBlankRow();
        writeString("Access Rights URL", datasetMetadata.getLinks().getAccessRights().getHref());
        writeString("Spatial URL", datasetMetadata.getLinks().getSpatial().getHref());
        writeString("Dataset Version URL", datasetMetadata.getLinks().getVersion().getHref());
    }

    private void writeLatestChanges() {

        if (datasetMetadata.getLatestChanges() == null)
            return;

        for (LatestChange change : datasetMetadata.getLatestChanges()) {

            writeBlankRow();
            writeString("Change Name", change.getName());
            writeString("Change Type", change.getType());
            writeString("Change Description", change.getDescription());
        }
    }

    private void writeDownloads() {

        if (datasetMetadata.getDownloads() == null)
            return;

        writeBlankRow();
        writeString("XLSX URL", datasetMetadata.getDownloads().getXls().getUrl());
        writeString("XLSX File Size (bytes)", datasetMetadata.getDownloads().getXls().getSize());

        writeBlankRow();
        writeString("CSV URL", datasetMetadata.getDownloads().getCsv().getUrl());
        writeString("CSV File Size (bytes)", datasetMetadata.getDownloads().getCsv().getSize());
    }

    private void writeDistributions() {

        if (datasetMetadata.getDistribution() == null)
            return;

        writeBlankRow();
        for (String distribution : datasetMetadata.getDistribution()) {
            writeString("Distribution", distribution);
        }
    }

    private void writeCodeLists() {

        if (datasetMetadata.getDimensions() == null)
            return;

        for (CodeList codelist : datasetMetadata.getDimensions()) {
            writeBlankRow();
            writeString("Code List Name", codelist.getName());
            writeString("Code List Description", codelist.getDescription());
            writeString("Code List ID", codelist.getId());
            writeString("Code List URL", codelist.getHref());
        }
    }

    private void writeAlerts() {

        if (datasetMetadata.getAlerts() == null)
            return;

        for (Alert alert : datasetMetadata.getAlerts()) {

            writeBlankRow();
            writeString("Alert Date", alert.getDate());
            writeString("Alert Type", alert.getType());
            writeString("Alert Description", alert.getDescription());
        }
    }

    private void writeKeywords() {

        if (datasetMetadata.getKeywords() == null)
            return;

        writeBlankRow();
        for (String keyword : datasetMetadata.getKeywords()) {
            writeString("Keyword", keyword);
        }
    }

    private void writeContactDetails() {

        if (datasetMetadata.getContacts() == null)
            return;

        for (ContactDetails contact : datasetMetadata.getContacts()) {

            writeBlankRow();
            writeString("Contact Name", contact.getName());
            writeString("Contact Telephone", contact.getTelephone());
            writeString("Contact Email", contact.getEmail());
        }
    }

    private void writeIndividualValues() {

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
    }

    private void writeBlankRow() {
        sheet.createRow(rowOffset);
        rowOffset++;
    }

    private void writeBoolean(String title, Boolean value) {

        if (value == null)
            return;

        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(0);
        cell.setCellStyle(titleStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(valueStyle);

        String printValue = value ? "yes" : "no";
        cell.setCellValue(printValue);
        rowOffset++;
    }

    private void writeString(String title, String value) {

        if (value == null)
            return;

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
