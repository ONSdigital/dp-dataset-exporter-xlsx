package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by dave on 08/12/2017.
 */
public class Version {

    @JsonProperty("downloads")
    private DownloadsList downloadsList;

    public Version(DownloadsList downloadsList) {
        this.downloadsList = downloadsList;
    }

    public DownloadsList getDownloadsList() {
        return downloadsList;
    }

    public void setDownloadsList(DownloadsList downloadsList) {
        this.downloadsList = downloadsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        return new EqualsBuilder()
                .append(getDownloadsList(), version.getDownloadsList())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getDownloadsList())
                .toHashCode();
    }
}
