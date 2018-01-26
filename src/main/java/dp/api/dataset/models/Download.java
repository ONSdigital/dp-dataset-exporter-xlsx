package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Metadata for a single file download.
 */
public class Download {

    @JsonProperty("url")
    private String url;

    @JsonProperty("size")
    private String size;

    public Download(String url, String size) {
        this.url = url;
        this.size = size;
    }

    public Download() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Download download = (Download) o;

        return new EqualsBuilder()
                .append(getUrl(), download.getUrl())
                .append(getSize(), download.getSize())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUrl())
                .append(getSize())
                .toHashCode();
    }
}
