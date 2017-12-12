package dp.api.dataset.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The contact details for a dataset.
 */
public class ContactDetails {

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("telephone")
    private String telephone;

    public ContactDetails(String email, String name, String telephone) {
        this.email = email;
        this.name = name;
        this.telephone = telephone;
    }

    public ContactDetails() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
