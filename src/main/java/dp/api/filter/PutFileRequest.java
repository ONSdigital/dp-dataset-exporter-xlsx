package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PutFileRequest {

    private Downloads downloads;

    public PutFileRequest(Downloads downloads) {
        this.downloads = downloads;
    }

    public Downloads getDownloads() {
        return downloads;
    }

    public void setDownloads(Downloads downloads) {
        this.downloads = downloads;
    }
}