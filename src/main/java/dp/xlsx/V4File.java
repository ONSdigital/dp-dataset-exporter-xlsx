package dp.xlsx;

import au.com.bytecode.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A class used to extract information from a V4 file.
 */
class V4File {

    private final List<String[]> data;

    private final int headerOffset;

    private Set<String> uniqueTimeValues;

    V4File(final InputStream inputStream) throws IOException {
        try (final CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            data = reader.readAll();
        }
        final String[] header = data.get(0);
        final String v4Code = header[0];
        headerOffset = Integer.parseInt(v4Code.split("V4_")[1]) + 1;
        uniqueTimeValues = new HashSet<>();
    }

    /**
     * Get all unique time labels found in the v4 file
     *
     * @return
     */
    List<String> getUniqueTimeLabels() {
        return new ArrayList<>(uniqueTimeValues);
    }

    /**
     * Group the v4 file by all dimension except for the time dimension
     *
     * @return A list of all groups within the v4 file
     */
    List<Group> groupData() {
        data.remove(0);
        final Map<Group, Group> groups = new HashMap<>();
        data.stream().forEach(row -> {
            final Group group = new Group(row, headerOffset);
            final String timeValue = row[headerOffset + 1];
            final String observation = row[0];
            if (groups.containsKey(group)) {
                uniqueTimeValues.add(timeValue);
                groups.get(group).addObservation(timeValue, observation);
            } else {
                uniqueTimeValues.add(timeValue);
                group.addObservation(timeValue, observation);
                groups.put(group, group);
            }
        });

        return new ArrayList<>(groups.values());
    }
}


