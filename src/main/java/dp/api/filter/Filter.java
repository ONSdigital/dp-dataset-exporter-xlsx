package dp.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Filter {

    @JsonProperty("version")
    private FilterLinks links;

    public FilterLinks getLinks() {
        return links;
    }

    public void setLinks(FilterLinks links) {
        this.links = links;
    }
}


//    type Filter struct {
//        InstanceID  string      `bson:"instance_id"          json:"instance_id"`
//        Dimensions  []Dimension `bson:"dimensions,omitempty" json:"dimensions,omitempty"`
//        Downloads   *Downloads  `bson:"downloads,omitempty"  json:"downloads,omitempty"`
//        Events      Events      `bson:"events,omitempty"     json:"events,omitempty"`
//        FilterID    string      `bson:"filter_id"            json:"filter_id,omitempty"`
//        State       string      `bson:"state,omitempty"      json:"state,omitempty"`
//        Links       LinkMap     `bson:"links"                json:"links,omitempty"`
//        LastUpdated time.Time   `bson:"last_updated"         json:"-"`
//        }
//