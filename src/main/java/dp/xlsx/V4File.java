package dp.xlsx;

import au.com.bytecode.opencsv.CSVReader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class used to extract information from a V4 file.
 */
class V4File {

    private final List<String[]> data; // v4 CSV rows
    private final int headerOffset;
    final String[] header;

    private Set<String> uniqueTimeValues;


    V4File(final InputStream inputStream) throws IOException {
        try (final CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            data = reader.readAll();
        }

        header  = data.get(0);
        final String v4Code = header[0];
        headerOffset = Integer.parseInt(v4Code.split("V4_")[1]) + 1;
        uniqueTimeValues = new HashSet<>();
    }

    public String getDimensionsTitle() {

        final int labelOffset = 2; // Skip the code and get the label when iterating columns
        final int offset = headerOffset + 1; // an additional one for the observation column
        List<String> dimensions = new ArrayList<>();

        for (int i = offset; i < header.length; i += labelOffset) {
            dimensions.add(header[i]);
        }

        return dimensions.stream()
                .skip(1) // skip geography dimension
                .map(d -> StringUtils.capitalize(d))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Group the v4 file by all dimension except for the time dimension
     *
     * @return A list of all groups within the v4 file
     */
    List<Group> groupData() {

        final Map<Group, Group> groups = new HashMap<>();

        data.stream().skip(1).forEach(row -> {

            final Group group = new Group(row, header, headerOffset);
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
        Map<Date,String> dates = new TreeMap<>();

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


