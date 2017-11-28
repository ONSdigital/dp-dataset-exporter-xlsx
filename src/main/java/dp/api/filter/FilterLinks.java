package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import dp.api.Link;

public class FilterLinks {

    @JsonProperty("version")
    private Link version;

    public Link getVersion() {
        return version;
    }

    public void setVersion(Link version) {
        this.version = version;
    }
}


//// LinkMap contains a named LinkObject for each link to other resources
//        type LinkMap struct {
//        Dimensions      LinkObject `bson:"dimensions"                 json:"dimensions,omitempty"`
//        FilterOutput    LinkObject `json:"filter_output,omitempty"`
//        FilterBlueprint LinkObject `bson:"filter_blueprint,omitempty" json:"filter_blueprint,omitempty"`
//        Self            LinkObject `bson:"self"                       json:"self,omitempty"`
//        version         LinkObject `bson:"version"                    json:"version,omitempty"`
//        }
//