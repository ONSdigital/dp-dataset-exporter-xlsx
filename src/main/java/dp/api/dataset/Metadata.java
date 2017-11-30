package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private DatasetDownloads downloads;

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
    private Collection<GeneralDetails> related_datasets;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
