package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata provided with a link
 */
public class GeneralDetails {

    @JsonProperty("href")
    private String href;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    public GeneralDetails(String href, String title, String description) {
        this.href = href;
        this.title = title;
        this.description = description;
    }

    public GeneralDetails() {}

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
