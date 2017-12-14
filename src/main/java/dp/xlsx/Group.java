package dp.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single unique combination of dimension options, and its associated observations.
 */
public class Group {

    private List<String> groupValues; // the unique dimension options
    private Map<String, String> observations; // time: observation

    /**
     * Create a group of dimensions
     *
     * @param data   A row from a V4 file
     * @param offset The v4 file offset
     */
    Group(String[] data, int offset) {

        final int labelOffset = 2; // Skip the code and get the label when iterating columns
        groupValues = new ArrayList<>();
        observations = new HashMap<>();
        int columnOffset = offset + 3; // skip the observation, time code and time label

        // read geography code and label
        String value = data[columnOffset];

        if ("".equals(value)) {
            value = data[columnOffset - 1]; // Just use the code
        } else {
            value = String.format("%s (%s)", value, data[columnOffset - 1]); // Append the code to the label
        }

        getGroupValues().add(value);
        columnOffset += labelOffset;

        // add all other dimensions
        for (int i = columnOffset; i < data.length; i += labelOffset) {

            value = data[i];

            if ("".equals(value))
                value = data[i - 1]; // Just use the code

            getGroupValues().add(value);
        }
    }

    /**
     * Add a observation into the group
     *
     * @param timeLabel   The label used for the time
     * @param observation The observation value
     */
    void addObservation(final String timeLabel, final String observation) {
        observations.put(timeLabel, observation);
    }

    String getObservation(String time) {
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

    protected List<String> getGroupValues() {
        return this.groupValues;
    }
}
