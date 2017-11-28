package dp.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {

    @JsonProperty("id")
    private String id;

    @JsonProperty("href")
    private String href;

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