package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DownloadsList {

    @JsonProperty("XLS")
    private Download xls;

    @JsonProperty("CSV")
    private Download csv;

    public DownloadsList() {
    }

    public DownloadsList(Download xls, Download csv) {
        this.xls = xls;
        this.csv = csv;
    }

    public Download getXls() {
        return xls;
    }

    public void setXls(Download xls) {
        this.xls = xls;
    }

    public Download getCsv() {
        return csv;
    }

    public void setCsv(Download csv) {
        this.csv = csv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DownloadsList that = (DownloadsList) o;

        return new EqualsBuilder()
                .append(getXls(), that.getXls())
                .append(getCsv(), that.getCsv())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getXls())
                .append(getCsv())
                .toHashCode();
    }
}
