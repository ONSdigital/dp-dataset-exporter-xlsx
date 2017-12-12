package dp.xlsx;

import dp.api.dataset.models.Alert;
import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.ContactDetails;
import dp.api.dataset.models.GeneralDetails;
import dp.api.dataset.models.LatestChange;
import dp.api.dataset.models.Metadata;
import dp.api.dataset.models.MetadataLinks;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

class MetadataFormatter {

    private final Metadata datasetMetadata;
    private final CellStyle headerStyle;
    private final CellStyle textStyle;
    private final CellStyle linkStyle;
    private final Sheet sheet;

    private int rowOffset;

    private final CreationHelper createHelper; // used to create hyperlinks

    public MetadataFormatter(Sheet sheet, Metadata datasetMetadata, CellStyle headerStyle, CellStyle textStyle, CellStyle linkStyle) {
        this.datasetMetadata = datasetMetadata;
        this.headerStyle = headerStyle;
        this.textStyle = textStyle;
        this.linkStyle = linkStyle;
        this.sheet = sheet;

        createHelper = sheet.getWorkbook().getCreationHelper();
    }

    void format() {

        rowOffset = 0;

        writeIndividualValues();
        writeContactDetails();
        writeAlerts();
        writeLatestChanges();
        writeCodeLists();
        writeMethodologies();
        writePublications();
        writeQMI();
        writeRelatedDatasets();
        writeLinks();
        writeDownloads();

        sheet.setColumnWidth(0, 20 * 256); // 20 characters
        sheet.setColumnWidth(1, 80 * 256); // 80 characters
    }

    private void writeRelatedDatasets() {

        if (datasetMetadata.getRelatedDatasets() == null)
            return;

        writeHeader("Related datasets");

        for (GeneralDetails details : datasetMetadata.getRelatedDatasets()) {
            writeString("", details.getTitle());
            writeString("", details.getDescription());
            writeLink("", details.getHref());
            writeBlankRow();
        }
    }

    private void writeQMI() {

        if (datasetMetadata.getQmi() == null)
            return;

        writeHeader("Quality and methodology information");
        writeString("", datasetMetadata.getQmi().getTitle());
        writeString("", datasetMetadata.getQmi().getDescription());
        writeLink("", datasetMetadata.getQmi().getHref());
        writeBlankRow();
    }

    private void writePublications() {

        if (datasetMetadata.getPublications() == null)
            return;

        writeHeader("Publications that use this data");

        for (GeneralDetails details : datasetMetadata.getPublications()) {

            writeString("", details.getTitle());
            writeString("", details.getDescription());
            writeLink("", details.getHref());
            writeBlankRow();
        }
    }

    private void writeMethodologies() {

        if (datasetMetadata.getMethodologies() == null)
            return;

        writeHeader("Methodology");

        for (GeneralDetails details : datasetMetadata.getMethodologies()) {
            writeString("", details.getTitle());
            writeString("", details.getDescription());
            writeLink("", details.getHref());
            writeBlankRow();
        }
    }

    private void writeLinks() {

        MetadataLinks links = datasetMetadata.getLinks();
        if (links == null)
            return;

        writeHeader("Links");

        if (links.getAccessRights() != null)
            writeLink("Access Rights", links.getAccessRights().getHref());

        if (links.getSpatial() != null)
            writeLink("Spatial", links.getSpatial().getHref());

        if (links.getWebsiteVersion() != null)
            writeLink("Dataset Version", links.getWebsiteVersion().getHref());

        writeBlankRow();
    }

    private void writeLatestChanges() {

        if (datasetMetadata.getLatestChanges() == null)
            return;

        writeHeader("What has changed in this edition");

        for (LatestChange change : datasetMetadata.getLatestChanges()) {

            writeString("", change.getName());
            writeString("", change.getType());
            writeString("", change.getDescription());
            writeBlankRow();
        }
    }

    private void writeDownloads() {
        if (datasetMetadata.getDownloads() == null) {
            return;
        }

        if (datasetMetadata.getDownloads().getXls() != null) {
            writeLink("XLSX Download", datasetMetadata.getDownloads().getXls().getUrl());
            writeString("Size (bytes)", datasetMetadata.getDownloads().getXls().getSize());
            writeBlankRow();
        }

        if (datasetMetadata.getDownloads().getCsv() != null) {
            writeLink("CSV Download", datasetMetadata.getDownloads().getCsv().getUrl());
            writeString("Size (bytes)", datasetMetadata.getDownloads().getCsv().getSize());
            writeBlankRow();
        }
    }

    private void writeCodeLists() {

        if (datasetMetadata.getDimensions() == null)
            return;

        writeHeader("In this dataset");

        for (CodeList codelist : datasetMetadata.getDimensions()) {

            writeString("", StringUtils.capitalize(codelist.getName()));
            writeString("", codelist.getDescription());
            writeBlankRow();
        }
    }

    private void writeAlerts() {

        if (datasetMetadata.getAlerts() == null)
            return;

        writeHeader("Alerts");

        for (Alert alert : datasetMetadata.getAlerts()) {

            writeString("", alert.getDate());
            writeString("", alert.getType());
            writeString("", alert.getDescription());
            writeBlankRow();
        }
    }

    private void writeContactDetails() {

        if (datasetMetadata.getContacts() == null)
            return;

        writeHeader("Contacts");

        for (ContactDetails contact : datasetMetadata.getContacts()) {

            writeString("", contact.getName());
            writeString("", contact.getTelephone());
            writeString("", contact.getEmail());
            writeBlankRow();
        }
    }

    private void writeIndividualValues() {

        writeString("Dataset Title", datasetMetadata.getTitle(), headerStyle, headerStyle);
        writeString("Description", datasetMetadata.getDescription());
        writeString("Release Date", datasetMetadata.getReleaseDate());
        writeString("Next Release", datasetMetadata.getNextRelease());
        writeString("Release Frequency", datasetMetadata.getReleaseFrequency());
        writeLink("Dataset URL", datasetMetadata.getUri());
        writeString("Licence", datasetMetadata.getLicense());
        writeString("Theme", datasetMetadata.getTheme());
        writeString("Unit of Measure", datasetMetadata.getUnitOfMeasure());
        writeBoolean("National Statistic", datasetMetadata.getNationalStatistic());
        writeBlankRow();
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
        cell.setCellStyle(textStyle);
        cell.setCellValue(title);
        cell = row.createCell(1);
        cell.setCellStyle(textStyle);

        String printValue = value ? "Yes" : "No";
        cell.setCellValue(printValue);
        rowOffset++;
    }

    private void writeString(String title, String value) {
        writeString(title, value, textStyle, textStyle);
    }

    private void writeString(String title, String value, CellStyle titleStyle, CellStyle valueStyle) {

        if (StringUtils.isEmpty(value))
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

    private void writeLink(String value, String href) {

        if (StringUtils.isEmpty(href))
            return;

        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(0);
        cell.setCellStyle(textStyle);
        cell.setCellValue(value);

        cell = row.createCell(1);
        cell.setCellStyle(linkStyle);
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(href);
        cell.setHyperlink(link);
        cell.setCellValue(href);

        rowOffset++;
    }

    private void writeHeader(String header) {

        Row row = sheet.createRow(rowOffset);
        Cell cell = row.createCell(0);
        cell.setCellStyle(headerStyle);
        cell.setCellValue(header);
        rowOffset++;
    }
}
