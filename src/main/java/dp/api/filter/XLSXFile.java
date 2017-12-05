package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XLSXFile {

    private final String url;

    private final String size;

    public XLSXFile(String url, String size) {
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