package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for a code list.
 */
public class CodeList {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    @JsonProperty("description")
    private String description;

    @JsonProperty("href")
    private String href;

    public CodeList(String id, String name, String description, String href) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.href = href;
    }

    public CodeList() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    // getBestIdentifier will return label by preference, otherwise name
    public String getBestIdentifier() {
        if ("".equals(label) || label == null) {
            return name;
        } else {
            return label;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
