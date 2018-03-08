package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;

/**
 * Metadata returned from the dataset API related to a specific version.
 */
public class Metadata {

    @JsonProperty("alerts")
    private Collection<Alert> alerts;

    @JsonProperty("contacts")
    private Collection<ContactDetails> contacts;

    @JsonProperty("description")
    private String description;

    @JsonProperty("dimensions")
    private Collection<CodeList> dimensions;

    @JsonProperty("distribution")
    private Collection<String> distribution;

    @JsonProperty("downloads")
    private DownloadsList downloads;

    @JsonProperty("keywords")
    private Collection<String> keywords;

    @JsonProperty("latest_changes")
    private Collection<LatestChange> latestChanges;

    @JsonProperty("license")
    private String license;

    @JsonProperty("links")
    private MetadataLinks links;

    @JsonProperty("methodologies")
    private Collection<GeneralDetails> methodologies;

    @JsonProperty("national_statistic")
    private Boolean nationalStatistic;

    @JsonProperty("next_release")
    private String nextRelease;

    @JsonProperty("publications")
    private Collection<GeneralDetails> publications;

    @JsonProperty("publisher")
    private Publisher publisher;

    @JsonProperty("qmi")
    private GeneralDetails qmi;

    @JsonProperty("related_datasets")
    private Collection<GeneralDetails> relatedDatasets;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("release_frequency")
    private String releaseFrequency;

    @JsonProperty("temporal")
    private Collection<TemporalFrequency> temporal;

    @JsonProperty("theme")
    private String theme;

    @JsonProperty("title")
    private String title;

    @JsonProperty("unit_of_measure")
    private String unitOfMeasure;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("user_notes")
    private UserNotes[] userNotes;

    public Collection<GeneralDetails> getRelatedDatasets() {
        return relatedDatasets;
    }

    public void setRelatedDatasets(Collection<GeneralDetails> relatedDatasets) {
        this.relatedDatasets = relatedDatasets;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Collection<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(Collection<Alert> alerts) {
        this.alerts = alerts;
    }

    public Collection<ContactDetails> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<ContactDetails> contacts) {
        this.contacts = contacts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<CodeList> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Collection<CodeList> dimensions) {
        this.dimensions = dimensions;
    }

    public Collection<String> getDistribution() {
        return distribution;
    }

    public void setDistribution(Collection<String> distribution) {
        this.distribution = distribution;
    }

    public DownloadsList getDownloads() {
        return downloads;
    }

    public void setDownloads(DownloadsList downloads) {
        this.downloads = downloads;
    }

    public Collection<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Collection<String> keywords) {
        this.keywords = keywords;
    }

    public Collection<LatestChange> getLatestChanges() {
        return latestChanges;
    }

    public void setLatestChanges(Collection<LatestChange> latestChanges) {
        this.latestChanges = latestChanges;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public MetadataLinks getLinks() {
        return links;
    }

    public void setLinks(MetadataLinks links) {
        this.links = links;
    }

    public Collection<GeneralDetails> getMethodologies() {
        return methodologies;
    }

    public void setMethodologies(Collection<GeneralDetails> methodologies) {
        this.methodologies = methodologies;
    }

    public Boolean getNationalStatistic() {
        return nationalStatistic;
    }

    public void setNationalStatistic(Boolean nationalStatistic) {
        this.nationalStatistic = nationalStatistic;
    }

    public String getNextRelease() {
        return nextRelease;
    }

    public void setNextRelease(String nextRelease) {
        this.nextRelease = nextRelease;
    }

    public Collection<GeneralDetails> getPublications() {
        return publications;
    }

    public void setPublications(Collection<GeneralDetails> publications) {
        this.publications = publications;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public GeneralDetails getQmi() {
        return qmi;
    }

    public void setQmi(GeneralDetails qmi) {
        this.qmi = qmi;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseFrequency() {
        return releaseFrequency;
    }

    public void setReleaseFrequency(String releaseFrequency) {
        this.releaseFrequency = releaseFrequency;
    }

    public Collection<TemporalFrequency> getTemporal() {
        return temporal;
    }

    public void setTemporal(Collection<TemporalFrequency> temporal) {
        this.temporal = temporal;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UserNotes[] getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(UserNotes[] userNotes) {
        this.userNotes = userNotes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Metadata metadata = (Metadata) o;

        return new EqualsBuilder()
                .append(getAlerts(), metadata.getAlerts())
                .append(getContacts(), metadata.getContacts())
                .append(getDescription(), metadata.getDescription())
                .append(getDimensions(), metadata.getDimensions())
                .append(getDistribution(), metadata.getDistribution())
                .append(getDownloads(), metadata.getDownloads())
                .append(getKeywords(), metadata.getKeywords())
                .append(getLatestChanges(), metadata.getLatestChanges())
                .append(getLicense(), metadata.getLicense())
                .append(getLinks(), metadata.getLinks())
                .append(getMethodologies(), metadata.getMethodologies())
                .append(getNationalStatistic(), metadata.getNationalStatistic())
                .append(getNextRelease(), metadata.getNextRelease())
                .append(getPublications(), metadata.getPublications())
                .append(getPublisher(), metadata.getPublisher())
                .append(getQmi(), metadata.getQmi())
                .append(getRelatedDatasets(), metadata.getRelatedDatasets())
                .append(getReleaseDate(), metadata.getReleaseDate())
                .append(getReleaseFrequency(), metadata.getReleaseFrequency())
                .append(getTemporal(), metadata.getTemporal())
                .append(getTheme(), metadata.getTheme())
                .append(getTitle(), metadata.getTitle())
                .append(getUnitOfMeasure(), metadata.getUnitOfMeasure())
                .append(getUri(), metadata.getUri())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getAlerts())
                .append(getContacts())
                .append(getDescription())
                .append(getDimensions())
                .append(getDistribution())
                .append(getDownloads())
                .append(getKeywords())
                .append(getLatestChanges())
                .append(getLicense())
                .append(getLinks())
                .append(getMethodologies())
                .append(getNationalStatistic())
                .append(getNextRelease())
                .append(getPublications())
                .append(getPublisher())
                .append(getQmi())
                .append(getRelatedDatasets())
                .append(getReleaseDate())
                .append(getReleaseFrequency())
                .append(getTemporal())
                .append(getTheme())
                .append(getTitle())
                .append(getUnitOfMeasure())
                .append(getUri())
                .toHashCode();
    }
}
