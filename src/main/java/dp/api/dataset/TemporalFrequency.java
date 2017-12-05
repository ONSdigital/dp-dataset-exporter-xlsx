package dp.api.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TemporalFrequency represents a frequency for a particular period of time.
 */
public class TemporalFrequency {

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("frequency")
    private String frequency;

    @JsonProperty("start_date")
    private String startDate;

    public TemporalFrequency(String endDate, String frequency, String startDate) {
        this.endDate = endDate;
        this.frequency = frequency;
        this.startDate = startDate;
    }

    public TemporalFrequency() {}

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
