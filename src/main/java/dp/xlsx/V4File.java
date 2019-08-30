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
    private final Map<String, String> timeLabels;
    private Group headerGroup;
    private String[] additionalHeaders;

    V4File(final InputStream inputStream, Metadata datasetMetadata) throws IOException {

        final Map<Group, Group> groups = new HashMap<>();
        final GroupExtractor groupExtractor = new GroupExtractor();

        try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            timeLabels = new HashMap<String, String>();

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
                headerGroup = groupExtractor.extractHeaderRow(header, headerOffset, datasetMetadata);

                additionalHeaders = Arrays.copyOfRange(header, 1, headerOffset);
            }

            while ((line = bufferedReader.readLine()) != null) {

                final String[] row = parser.parseLine(line);
                if (row == null || row.length == 0) {
                    continue;
                }

                final Group group = groupExtractor.extractObsRow(row, headerOffset);
                final String timeValue = row[headerOffset];
                final String timeLabel = row[headerOffset+1];
                final String observation = row[0];
                final String additionalData[] = Arrays.copyOfRange(row, 1, headerOffset);

                timeLabels.put(timeValue, timeLabel);
                if (groups.containsKey(group)) {
                    groups.get(group).addObservation(timeLabel, observation, additionalData);
                } else {
                    group.addObservation(timeLabel, observation, additionalData);
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
    TreeMap<String, String> getUniqueTimeLabels() {
        return new TreeMap<String, String>(timeLabels);
    }

    /**
     * Return the time labels in chronological order if the format is recognised, else
     * returns the labels in alphabetical order.
     *
     * @return
     */
    Collection<String> getOrderedTimeLabels() {
        String first = timeLabels.entrySet().iterator().next().getKey();
        String format = DateLabel.determineDateFormat(first);

        // if the format is not recognised - return alphabetically (treemap naturally sorts)
        if (StringUtils.isEmpty(format)) {
            TreeMap<String, String> timeLabels = getUniqueTimeLabels();
            return timeLabels.values();
        }

        DateFormat dateFormat = new SimpleDateFormat(format);
        Map<Date, String> dates = new TreeMap<>();

        for (Map.Entry<String, String> time : timeLabels.entrySet()) {
            Date date;
            try {
                date = dateFormat.parse(time.getKey());
            } catch (ParseException e) {
                date = new Date(); // cannot sort it if we cannot parse it.
            }

            dates.put(date, time.getKey());
        }

        for (Map.Entry<Date, String> date : dates.entrySet()) {
            dates.put(date.getKey(), timeLabels.get(date.getValue()));
        }

        return dates.values();
    }

    public String[] getAdditionalHeaders() {
        return additionalHeaders;
    }
}