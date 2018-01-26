package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LatestChange {

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    public LatestChange(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public LatestChange() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}