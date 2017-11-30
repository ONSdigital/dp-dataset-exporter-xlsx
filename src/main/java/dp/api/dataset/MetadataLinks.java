package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;
import dp.api.Link;

/**
 * Links provided with dataset metadata.
 */
public class MetadataLinks {

    @JsonProperty("access_rights")
    private Link accessRights;

    @JsonProperty("self")
    private Link self;

    @JsonProperty("spatial")
    private Link spatial;

    @JsonProperty("version")
    private Link version;

    public MetadataLinks(Link accessRights, Link self, Link spatial, Link version) {
        this.accessRights = accessRights;
        this.self = self;
        this.spatial = spatial;
        this.version = version;
    }

    public Link getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(Link accessRights) {
        this.accessRights = accessRights;
    }

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getSpatial() {
        return spatial;
    }

    public void setSpatial(Link spatial) {
        this.spatial = spatial;
    }

    public Link getVersion() {
        return version;
    }

    public void setVersion(Link version) {
        this.version = version;
    }
}
