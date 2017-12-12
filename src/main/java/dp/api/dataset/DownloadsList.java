package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DownloadsList {

    @JsonProperty("XLS")
    private Download xls;

    @JsonProperty("CSV")
    private Download csv;

    public DownloadsList(Download xls, Download csv) {
        this.xls = xls;
        this.csv = csv;
    }

    public Download getXls() {
        return xls;
    }

    public Download getCsv() {
        return csv;
    }
}
