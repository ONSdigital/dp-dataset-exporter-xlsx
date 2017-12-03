package dp.xlsx;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

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
    public void groupTitles() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream);
            final Group group = file.groupData().get(0);
            assertThat(group.getTitle()).isEqualTo("K02000001\n" + "CPI (overall index) (cpi1dim1A0)");
        }
    }

    @Test
    public void timeTitles() throws IOException {
        try (final InputStream stream = V4FileTest.class.getResourceAsStream("v4_0.csv")) {
            final V4File file = new V4File(stream);
            final Group group = file.groupData().get(0);
            assertThat(group.getTitle()).isEqualTo("K02000001\n" + "CPI (overall index) (cpi1dim1A0)");
        }
    }
}
