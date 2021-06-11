package dp.xlsx;

import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.Metadata;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group implements Comparable<Group> {

    private List<DimensionData> groupValues; // the unique dimension options
    private Map<String, Observation> observations; // <time, observation>

    /**
     * Construct a new Group from the provided parameters.
     */
    Group(final List<DimensionData> groupValues, final Map<String, Observation> observations) {
        this.groupValues = groupValues;
        this.observations = observations;
    }

    /**
     * Gather relevant cells from a csv row. There is a variation of approach for a
     * header row vs an observational data row (literally everything that's not a header row),
     * the two are differentiated by the presence of metadata (only the header row has this).
     *
     * @param data   A row from a V4 file
     * @param offset The v4 file offset
     */
    Group(String[] data, int offset, Metadata datasetMetadata) {

        final int labelOffset = 2; // Skip the code and get the label when iterating columns
        groupValues = new ArrayList<>();
        observations = new HashMap<>();
        int columnOffset = offset + 3; // skip the observation, time code and time label

        // add all other dimensions
        for (int i = columnOffset; i < data.length; i += labelOffset) {

            String label = data[i];

            // if this row has metadata its a header row ...
            // the label will need to be overwritten where a better name has been provided.
            if (datasetMetadata != null) {

                for (CodeList codelist : datasetMetadata.getDimensions()) {
                    if (codelist.getName().equals(label)) {
                        label = codelist.getBestIdentifier();
                    }
                }
            }

            String code = data[i - 1];

            if (i == columnOffset) {
                getGroupValues().add(new DimensionData(DimensionType.GEOGRAPHY, label, code));
            } else {
                getGroupValues().add(new DimensionData(DimensionType.OTHER, label, code));
            }
        }
    }

    /**
     * Add a observation into the group
     *
     * @param timeLabel        The label used for the time
     * @param observation      The observation value
     * @param additionalValues The addition values which are related to the observation
     */
    void addObservation(final String timeLabel, final String observation, final String[] additionalValues) {
        observations.put(timeLabel, new Observation(observation, additionalValues));
    }

    Observation getObservation(String time) {
        return observations.get(time);
    }

    @Override
    public int hashCode() {
        return getGroupValues().hashCode();
    }


    /**
     * 2 groups are considered to be equal if their {@link Group#getGroupValues()} are equal. The obeservation values
     * are not considered as part of the equality check.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || this.getClass() != o.getClass()) return false;

        final Group group = (Group) o;

        return new EqualsBuilder().append(this.getGroupValues(), group.getGroupValues()).isEquals();
    }

    protected List<DimensionData> getGroupValues() {
        return this.groupValues;
    }

    @Override
    public int compareTo(Group o) {

        int compared = 0;

        // if the group arrays differ in length do not try and compare.
        if (getGroupValues().size() != o.getGroupValues().size())
            return 0;

        // Order by each group value in turn
        for (int i = 0; i < getGroupValues().size(); ++i) {

            compared = getGroupValues().get(i).compareTo(o.getGroupValues().get(i));

            // return if we can determine order from this dimension option
            if (compared != 0)
                return compared;
        }

        return compared;
    }

    public Map<String, Observation> getObservations() {
        return this.observations;
    }
}
