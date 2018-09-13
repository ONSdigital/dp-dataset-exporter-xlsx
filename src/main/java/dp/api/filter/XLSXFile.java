package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XLSXFile {


    @JsonProperty("public")
    private String publicUrl;

    @JsonProperty("private")
    private String privateUrl;

    @JsonProperty("size")
    private String size;

    @JsonProperty("skipped")
    private Boolean skipped;

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getPrivateUrl() {
        return privateUrl;
    }

    public void setPrivateUrl(String privateUrl) {
        this.privateUrl = privateUrl;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Boolean getSkipped() {
        return skipped;
    }

    public void setSkipped(Boolean skipped) {
        this.skipped = skipped;
    }
}
