package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
