package dp.xlsx;

import au.com.bytecode.opencsv.CSVReader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A class used to extract information from a V4 file.
 */
class V4File {

    private final List<String[]> data; // v4 CSV rows
    private final int headerOffset;
    private final String[] header;

    private Set<String> uniqueTimeValues;


    V4File(final InputStream inputStream) throws IOException {
        try (final CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            data = reader.readAll();
        }

        header = data.get(0);
        final String v4Code = header[0];
        headerOffset = Integer.parseInt(v4Code.split("V4_")[1]) + 1;
        uniqueTimeValues = new HashSet<>();
    }

    /**
     * Group the v4 file by all dimension except for the time dimension
     *
     * @return A list of all groups within the v4 file
     */
    List<Group> groupData() {

        data.remove(0); // remove header

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

    /**
     * Return the list of dimensions that will be displayed along the columns of the XLSX output.
     * (not including the time dimension or whichever other dimension is not shown along the rows.)
     * @return
     */
    List<String> getDimensions() {

        int offset = headerOffset + 3; // skip the observation, time code and time label
        final int labelOffset = 2; // Skip the code and get the label when iterating columns
        List<String> dimensions = new ArrayList<>();

        for (int i = offset; i < header.length; i += labelOffset) {
            dimensions.add(header[i]);
        }

        return dimensions;
    }

    /**
     * Return a map containing the original position of the dimensions mapped to their sorted position.
     * This is done once to save having to sort each heading of the XLSX columns by dimension names.
     * By calculating the mapping once we can app
     */
    public Map<Integer, Integer> getSortedPositionMapping() {

        List<String> dimensions = getDimensions();
        Set<String> sortedDimensions = new TreeSet<>();

        Map<Integer, Integer> positionMappings = new HashMap<>();

        // populate a sorted set of dimensions.
        for (String dimension : dimensions) {
            sortedDimensions.add(dimension);
        }

        // determine where the new position of each dimension is after sorting.
        for (int i = 0; i < dimensions.size(); ++i) {

            int j = 0;
            Iterator<String> iterator = sortedDimensions.iterator();

            while (iterator.hasNext()) {
                if (iterator.next().equals(dimensions.get(i))) {
                    positionMappings.put(i, j);
                    break;
                }
                j++;
            }
        }

        return positionMappings;
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
     * Return the time labels in chronological order if the format is recognised, else
     * returns the labels in alphabetical order.
     *
     * @return
     */
    Collection<String> getOrderedTimeLabels() {

        String first = uniqueTimeValues.iterator().next();
        String format = DateLabel.determineDateFormat(first);

        // if the format is not recognised - just sort alphabetically.
        if (StringUtils.isEmpty(format)) {
            List<String> timeLabels = getUniqueTimeLabels();
            Collections.sort(timeLabels);
            return timeLabels;
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        Map<Date, String> dates = new TreeMap<>();

        for (String timeValue : uniqueTimeValues) {
            Date date;
            try {
                date = dateFormat.parse(timeValue);
            } catch (ParseException e) {
                date = new Date(); // cannot sort it if we cannot parse it.
            }

            dates.put(date, timeValue);
        }

        return dates.values();
    }
}


