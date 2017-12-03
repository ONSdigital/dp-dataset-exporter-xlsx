package dp.xlsx;

import java.util.*;

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
        groupValues = new ArrayList<>();
        observations = new HashMap<>();
        int skipTime = offset + 3; // skip the observation, time code and time label

        final int labelOffset = 2; // Skip the code and get the label
        for (int i = skipTime; i < data.length; i += labelOffset) {
            String value = data[i];
            if ("".equals(value)) value = data[i - 1]; // Get the code
            groupValues.add(value);
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
        groupValues.forEach(v -> buffer.append(v).append("\n"));
        final int size = buffer.toString().length();
        return buffer.toString().substring(0, size - 1);
    }

    String getObservation(String time) {
        return observations.get(time);
    }

    public Set<String> getTimeTitles() {
        return observations.keySet();
    }

    @Override
    public int hashCode() {
        return groupValues.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return this.hashCode() == object.hashCode();
    }


}
