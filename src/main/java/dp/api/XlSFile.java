package dp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XlSFile {

    private final String url;

    private final String size;

    public XlSFile(String url, String size) {
        this.url = url;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public String getSize() {
        return size;
    }
}