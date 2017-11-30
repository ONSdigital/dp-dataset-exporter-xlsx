package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for a single file download.
 */
public class Download {

    @JsonProperty("url")
    private String url;

    @JsonProperty("size")
    private String size;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
