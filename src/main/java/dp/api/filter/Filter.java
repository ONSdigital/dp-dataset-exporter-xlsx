package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A filter document provided by the filter API.
 */
public class Filter {

    @JsonProperty("links")
    private FilterLinks links;

    @JsonProperty("published")
    private boolean published;

    public FilterLinks getLinks() {
        return links;
    }

    public void setLinks(FilterLinks links) {
        this.links = links;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}