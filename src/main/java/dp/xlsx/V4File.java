package dp.xlsx;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import dp.api.dataset.models.Metadata;

/**
 * A class used to extract information from a V4 file.
 */
class V4File {

    private final Collection<Group> groupData;
    private final Set<String> uniqueTimeValues;
    private Group headerGroup;
    private String[] additionalHeaders;

    V4File(final InputStream inputStream, Metadata datasetMetadata) throws IOException {

        final Map<Group, Group> groups = new HashMap<>();
        final GroupProcessor processor = new GroupProcessor();

        try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            uniqueTimeValues = new HashSet<>();

            String line;
            int headerOffset = 0;

            CsvParserSettings settings = new CsvParserSettings();
            settings.setEmptyValue("");
            settings.setNullValue("");
            CsvParser parser = new CsvParser(settings);

            if ((line = bufferedReader.readLine()) != null) {

                final String[] header = parser.parseLine(line);
                if (header == null || header.length == 0) {
                    throw new IOException("header row does not contain any content");
                }

                final String v4Code = header[0];

                headerOffset = Integer.parseInt(v4Code.split("_")[1]) + 1;
                headerGroup = processor.processHeaderRow(header, headerOffset, datasetMetadata);

                additionalHeaders = Arrays.copyOfRange(header, 1, headerOffset);
            }

            while ((line = bufferedReader.readLine()) != null) {

                final String[] row = parser.parseLine(line);
                if (row == null || row.length == 0) {
                    continue;
                }

                final Group group = processor.processObsRow(row, headerOffset);
                final String timeValue = row[headerOffset + 1];
                final String observation = row[0];
                final String additionalData[] = Arrays.copyOfRange(row, 1, headerOffset);

                if (groups.containsKey(group)) {
                    uniqueTimeValues.add(timeValue);
                    groups.get(group).addObservation(timeValue, observation, additionalData);
                } else {
                    uniqueTimeValues.add(timeValue);
                    group.addObservation(timeValue, observation, additionalData);
                    groups.put(group, group);
                }
            }

            if (groups.size() < 1) {
                throw new IOException("Two or more csv rows are need to generate a XLSX file");
            }

            groupData = groups.values();
        }
    }

    /**
     * Group the v4 file by all dimension except for the time dimension
     *
     * @return A list of all groups within the v4 file
     */
    Collection<Group> groupData() {
        return groupData;
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

    public String[] getAdditionalHeaders() {
        return additionalHeaders;
    }
}