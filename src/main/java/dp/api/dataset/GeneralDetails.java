package dp.api.dataset;

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
