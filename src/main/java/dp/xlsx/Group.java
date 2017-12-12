package dp.xlsx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A single unique combination of dimension options, and its associated observations.
 */
public class Group implements Comparable<Group> {

    private Map<String, String> dimensionOptions; // the unique dimension options
    private Map<String, String> observations; // time: observation

    /**
     * Create a group of dimensions
     *
     * @param data   A row from a V4 file
     * @param offset The v4 file offset
     */
    Group(String[] data, String[] header, int offset) {

        final int labelOffset = 2; // Skip the code and get the label when iterating columns
        dimensionOptions = new TreeMap<>();
        observations = new HashMap<>();
        int columnOffset = offset + 3; // skip the observation, time code and time label

        String dimension = header[columnOffset];
        String option = data[columnOffset];

        if ("".equals(option)) {
            option = data[columnOffset - 1]; // Just use the code
        } else {
            option = String.format("%s (%s)", option, data[columnOffset - 1]); // Append the code to the label
        }

        dimensionOptions.put(dimension, option);
        columnOffset += labelOffset;

        // add all other dimensions
        for (int i = columnOffset; i < data.length; i += labelOffset) {

            dimension = header[i];
            option = data[i];

            if ("".equals(option))
                option = data[i - 1]; // Just use the code

            dimensionOptions.put(dimension, option);
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

    String getTitle() {
        StringBuffer buffer = new StringBuffer();
        dimensionOptions.values().forEach(v -> buffer.append(v).append("\n"));
        final int size = buffer.toString().length();
        return buffer.toString().substring(0, size - 1);
    }

    String getObservation(String time) {
        return observations.get(time);
    }

    @Override
    public int hashCode() {
        return dimensionOptions.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return this.hashCode() == object.hashCode();
    }

    @Override
    public int compareTo(Group o) {

        int compared = 0;

        // if the group arrays differ in length do not try and compare.
        List<String> values = new ArrayList<>(this.dimensionOptions.values());
        List<String> oValues = new ArrayList<>(o.dimensionOptions.values());

        if (values.size() != o.dimensionOptions.values().size())
            return 0;

        // Order by each group value in turn
        for (int i = 0; i < dimensionOptions.values().size(); ++i) {

            compared = values.get(i).compareTo(oValues.get(i));

            // return if we can determine order from this dimension option
            if (compared != 0)
                return compared;
        }

        return compared;
    }
}
