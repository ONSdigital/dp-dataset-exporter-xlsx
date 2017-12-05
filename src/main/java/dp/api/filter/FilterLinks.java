package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import dp.api.Link;

/**
 * The links contained within a filter document from the filter API.
 */
public class FilterLinks {

    @JsonProperty("version")
    private Link version;

    public Link getVersion() {
        return version;
    }

    public void setVersion(Link version) {
        this.version = version;
    }

}