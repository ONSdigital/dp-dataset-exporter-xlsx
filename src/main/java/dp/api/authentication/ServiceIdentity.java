package dp.api.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceIdentity {

    @JsonProperty("identifier")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
