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

	@JsonProperty("description")
	private String description;

	@JsonProperty("href")
	private String href;
	
	@JsonProperty("label")
	private String label;

	public CodeList(String id, String name, String description, String href, String label) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.href = href;
		this.label = label;
	}

	public CodeList() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
