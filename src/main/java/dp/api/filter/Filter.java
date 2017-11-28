package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A filter document provided by the filter API.
 */
public class Filter {

    @JsonProperty("version")
    private FilterLinks links;

    public FilterLinks getLinks() {
        return links;
    }

    public void setLinks(FilterLinks links) {
        this.links = links;
    }
}