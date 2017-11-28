package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata returned from the dataset API related to a specific version.
 */
public class Metadata {


    @JsonProperty("title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
