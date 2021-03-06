package dp.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A generic link representation as used throughout the DP API's.
 */
public class Link {

    @JsonProperty("id")
    private String id;

    @JsonProperty("href")
    private String href;

    public Link(String id, String href) {
        this.id = id;
        this.href = href;
    }

    public Link() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}