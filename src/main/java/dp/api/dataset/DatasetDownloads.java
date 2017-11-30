package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The set of downloads that are available for a dataset.
 */
public class DatasetDownloads {

    @JsonProperty("csv")
    private Download csv;

    @JsonProperty("xls")
    private Download xls;

    public Download getCsv() {
        return csv;
    }

    public void setCsv(Download csv) {
        this.csv = csv;
    }

    public Download getXls() {
        return xls;
    }

    public void setXls(Download xls) {
        this.xls = xls;
    }
}
