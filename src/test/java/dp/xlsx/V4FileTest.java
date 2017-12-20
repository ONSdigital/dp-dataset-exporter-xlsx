package dp.xlsx;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class V4FileTest {

    @Test
    public void groupSize() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream);
            assertThat(file.groupData().size()).isEqualTo(6);
        }
    }

    @Test
    public void groupContainsObservations() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream);
            final Group group = file.groupData().get(0);
            assertThat(group.getObservation("Jan-96")).isEqualTo("86.8");
            assertThat(group.getObservation("Feb-96")).isEqualTo("86.9");
        }
    }

    @Test
    public void orderedTimeLabels() throws IOException {

        // Given v4 data with time labels in a recognised format.
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow1 = "88,Month,Oct-00,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "88,Month,Jan-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow3 = "88,Month,Apr-17,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow1 + csvRow2 + csvRow3;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        file.groupData();

        // When getOrderedTimeLabels is called.
        List<String> labels = new ArrayList<>(file.getOrderedTimeLabels());

        // Then the labels are provided in chronological order.
        assertThat(labels.get(0)).isEqualTo("Jan-96");
        assertThat(labels.get(1)).isEqualTo("Oct-00");
        assertThat(labels.get(2)).isEqualTo("Apr-17");
    }

    @Test
    public void orderedTimeLabels_unrecognisedFormat() throws IOException {

        // Given v4 data with time labels in an unrecognised format.
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow1 = "88,Month,10-00,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "88,Month,01-96,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow3 = "88,Month,11-17,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow1 + csvRow2 + csvRow3;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        file.groupData();

        // When getOrderedTimeLabels is called.
        List<String> labels = new ArrayList<>(file.getOrderedTimeLabels());

        // Then the labels are provided in alphabetical order.
        assertThat(labels.get(0)).isEqualTo("01-96");
        assertThat(labels.get(1)).isEqualTo("10-00");
        assertThat(labels.get(2)).isEqualTo("11-17");
    }

    @Test
    public void getDimensions_ReturnsAllButFirstTimeDimension() throws Exception {

        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow1 = "88,Month,10-00,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow1;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        List<DimensionData> dimensions = file.getDimensions();

        assertThat(dimensions.get(0).getValue()).isEqualTo("Geography");
        assertThat(dimensions.get(1).getValue()).isEqualTo("Aggregate");
    }

    @Test
    public void getSortedPositionMapping() throws Exception {

        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow1 = "88,Month,10-00,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow1;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream);
        Map<Integer, Integer> sortedPositionMapping = file.getSortedPositionMapping();

        assertThat(sortedPositionMapping.get(0)).isEqualTo(1);
        assertThat(sortedPositionMapping.get(1)).isEqualTo(0);
    }
}
