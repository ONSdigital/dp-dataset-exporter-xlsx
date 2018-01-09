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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A class used to extract information from a V4 file.
 */
class V4File {

    private final List<String[]> data; // v4 CSV rows
    private final int headerOffset;
    private final Group headerGroup;
    private Set<String> uniqueTimeValues;

    V4File(final InputStream inputStream) throws IOException {
        try (final CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            data = reader.readAll();
            if (data.size() <= 1) {
                throw new IOException("Two or more csv rows are need to generate a XLSX file");
            }
        }

        final String[] header = data.get(0);
        final String v4Code = header[0];

        headerOffset = Integer.parseInt(v4Code.split("V4_")[1]) + 1;
        uniqueTimeValues = new HashSet<>();
        headerGroup = new Group(header, headerOffset);
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
     *
     * @return
     */
    List<DimensionData> getDimensions() {

        return headerGroup.getGroupValues();
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


