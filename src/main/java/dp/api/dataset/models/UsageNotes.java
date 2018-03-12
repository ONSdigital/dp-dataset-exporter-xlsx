package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsageNotes {

    @JsonProperty("title")
    private String title;

    @JsonProperty("note")
    private String notes;

    public String getTitle() {
        return title;
    }

    public String getNotes() {
        return notes;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
