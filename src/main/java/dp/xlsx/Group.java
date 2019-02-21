package dp.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.Metadata;

public class Group implements Comparable<Group> {

    private List<DimensionData> groupValues; // the unique dimension options
    private Map<String, Observation> observations; // <time, observation>

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

        // read geography code and label
        String label = data[columnOffset];
        String code = data[columnOffset -1];

        getGroupValues().add(new DimensionData(DimensionType.GEOGRAPHY, label, code));
        columnOffset += labelOffset;

        // add all other dimensions
        for (int i = columnOffset; i < data.length; i += labelOffset) {

            // if this row has metadata its a header row ...
            // the label will need to be overwritten where a better name has been provided.
            if (datasetMetadata != null) {

                for (CodeList codelist : datasetMetadata.getDimensions()) {
                    if (codelist.getName() == label) {
                        label = codelist.getBestIdentifier();
                    }
                }

            } else {
                label = data[i];
            }

            code = data[i -1];
            getGroupValues().add(new DimensionData(DimensionType.OTHER, label, code));
        }
    }

    /**
     * Add a observation into the group
     *
     * @param timeLabel   The label used for the time
     * @param observation The observation value
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

    @Override
    public boolean equals(Object object) {
        return this.hashCode() == object.hashCode();
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
}
