package dp.xlsx;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DimensionDataTest {

    @Test
    public void checkHashCode() {
        final DimensionData data1 = new DimensionData(DimensionType.GEOGRAPHY, "West Midlands", "E12000005");
        final DimensionData data2 = new DimensionData(DimensionType.GEOGRAPHY, "West Midlands", "E11000005");
        assertThat(data1.hashCode()).isNotEqualTo(data2.hashCode());
    }
}
