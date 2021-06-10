package dp.xlsx;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupTest {

    @Test
    public void test_hashCode_verySimilarDimensions_shouldHaveDifferentCodes() throws Exception {
        List<DimensionData> rowOne = new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "18", "18"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1990s", "1990s"));
        }};

        List<DimensionData> rowTwo = new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "19", "19"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1980s", "1980s"));
        }};

        Group groupOne = new Group(rowOne, null);
        Group groupTwo = new Group(rowTwo, null);

        assertThat(groupOne.hashCode()).isNotEqualTo(groupTwo.hashCode());
    }

    @Test
    public void test_hashCode_sameDimensions_shouldHaveTheSameCode() throws Exception {
        List<DimensionData> rowOne = new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "18", "18"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1990s", "1990s"));
        }};

        List<DimensionData> rowTwo = new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "18", "18"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1990s", "1990s"));
        }};

        Group groupOne = new Group(rowOne, null);
        Group groupTwo = new Group(rowTwo, null);

        assertThat(groupOne.hashCode()).isEqualTo(groupTwo.hashCode());
    }
}
