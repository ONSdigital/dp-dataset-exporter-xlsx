package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An alert associated with a dataset version.
 */
public class Alert {

    @JsonProperty("date")
    private String date;

    @JsonProperty("description")
    private String description;

    @JsonProperty("type")
    private String type;

    public Alert(String date, String description, String type) {
        this.date = date;
        this.description = description;
        this.type = type;
    }

    public Alert() {}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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