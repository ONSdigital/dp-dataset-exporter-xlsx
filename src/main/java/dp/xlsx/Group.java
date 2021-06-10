package dp.xlsx;

import dp.api.dataset.models.CodeList;
import dp.api.dataset.models.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        /**
         * Generate a string representation of the group by concatinating the string values of each
         * {@link DimensionData}. The hashcode of this string is the hashcode for this object.
         *
         * Explanation:
         * The previous hashcode implementation for {@link Group} used the hash code of the
         * {@link Group}{@link #getGroupValues()} but this hashcode was not specific enough when two
         * {@link DimensionData} objects had very similar values.
         *
         * The Group objects created from the following CSV rows although different were generating the same hashcode
         * value:
         *
         *      ,.,1978-to-2018-19,1978 to 2018-19,K02000001,United Kingdom,18,18,gross-income,Gross income,1990s,1990s
         *      ,.,1978-to-2018-19,1978 to 2018-19,K02000001,United Kingdom,19,19,gross-income,Gross income,1980s,1980s
         *
         * This hashcode collision resulted in the {@link V4File} incorrecting parsing the V4 file leading to missing
         * rows or rows with incorrect observations being assigned.
         * This slightly wierd approach attempts to address that problem. See GroupTest for me details.
         */
        return StringUtils.join(getGroupValues()
                .stream()
                .map(val -> val.toString())
                .collect(Collectors.toList()), "|")
                .hashCode();
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

    public Map<String, Observation> getObservations() {
        return this.observations;
    }
}
