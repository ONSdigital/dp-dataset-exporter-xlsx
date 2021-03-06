package dp.xlsx;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static dp.xlsx.DebugUtil.dimMatcher;
import static dp.xlsx.DebugUtil.filterGroups;
import static org.assertj.core.api.Assertions.assertThat;

public class V4FileTest {

    @Test
    public void groupSize() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream, null);
            assertThat(file.groupData().size()).isEqualTo(6);
        }
    }

    @Test(expected = IOException.class)
    public void v4File_InvalidHeader() throws IOException {

        // Given v4 data with an invalid header
        String csvHeader = "\n";
        String csvContent = csvHeader;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        // When the V4File constructor is called
        new V4File(inputStream, null);

        // Then the expected exception is thrown
    }

    @Test
    public void v4File_BlankLinesIgnored() throws IOException {

        // Given v4 data with a blank observation row
        String csvHeader = "V4_0,Time_codelist,Time,Geography_codelist,Geography,cpi1dim1aggid,Aggregate\n";
        String csvRow1 = "88,Month,Oct-00,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvRow2 = "\n";
        String csvRow3 = "88,Month,Apr-17,K02000001,,cpi1dim1A0,CPI (overall index)\n";
        String csvContent = csvHeader + csvRow1 + csvRow2 + csvRow3;

        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        final V4File file = new V4File(inputStream, null);
        file.groupData();

        // When getOrderedTimeLabels is called.
        List<String> labels = new ArrayList<>(file.getOrderedTimeLabels());

        // Then the labels are provided in chronological order with the blank row ignored
        assertThat(labels.get(0)).isEqualTo("Oct-00");
        assertThat(labels.get(1)).isEqualTo("Apr-17");
    }

    @Test
    public void groupContainsObservations() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream, null);
            final Optional<Group> group = file.groupData().stream().filter(g -> {
                for (DimensionData data : g.getGroupValues()) {
                    if (data.getValue().equals("02.1 Alcoholic beverages")) {
                        return true;
                    }
                }
                return false;
            }).findAny();
            assertThat(group.isPresent()).isEqualTo(true);
            assertThat(group.get().getObservation("Jan-96").getValue()).isEqualTo("95.6");
            assertThat(group.get().getObservation("Feb-96").getValue()).isEqualTo("95.9");
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
        final V4File file = new V4File(inputStream, null);
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
        final V4File file = new V4File(inputStream, null);
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
        final V4File file = new V4File(inputStream, null);
        List<DimensionData> dimensions = file.getDimensions();

        assertThat(dimensions.get(0).getValue()).isEqualTo("Geography");
        assertThat(dimensions.get(1).getValue()).isEqualTo("Aggregate");
    }

    @Test(expected = IOException.class)
    public void noResultsFound() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("no_data.csv")) {
            final V4File file = new V4File(stream, null);

        }
    }

    @Test
    public void shouldCreateTheExpectedGroupMapping() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_2_generational_income.csv")) {
            final V4File file = new V4File(stream, null);

            // 36 csv rows should translate to 36 groups for this dataset.
            assertThat(file.groupData().size()).isEqualTo(36);

            Predicate<Group> filter = dimMatcher("19", "19")
                    .and(dimMatcher("1980s", "1980s"))
                    .and(dimMatcher("gross-income", "Gross income"));

            List<Group> results = filterGroups(file.groupData(), filter);

            assertThat(results.size()).isEqualTo(1);
            assertThat(results.get(0).getObservation("1978 to 2018-19").getValue()).isEqualTo("14854");

            filter = dimMatcher("18", "18")
                    .and(dimMatcher("1990s", "1990s"))
                    .and(dimMatcher("gross-income", "Gross income"));

            results = filterGroups(file.groupData(), filter);

            assertThat(results.size()).isEqualTo(1);
            assertThat(results.get(0).getObservation("1978 to 2018-19").getValue()).isEqualTo("");
        }
    }

}
