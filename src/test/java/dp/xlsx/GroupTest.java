package dp.xlsx;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupTest {

    private Group indirectTaxesAge18The90s;
    private Group indirectTaxesAge19The80s;
    private Group clothing;
    private Group alcoholicBeverages;

    @Before
    public void setup() {
        indirectTaxesAge18The90s = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "18", "18"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1990s", "1990s"));
        }}, null);

        indirectTaxesAge19The80s = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "United Kingdom", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "19", "19"));
            add(new DimensionData(DimensionType.OTHER, "Indirect taxes", "indirect-taxes"));
            add(new DimensionData(DimensionType.OTHER, "1980s", "1980s"));
        }}, null);

        clothing = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "K02000001", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "cpi1dim1G30100", "03.1 Clothing"));
        }}, null);

        alcoholicBeverages = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "K02000001", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "cpi1dim1G20100", "02.1 Alcoholic beverages"));
        }}, null);
    }

    @Test
    public void testEquals_verySimilarDimensions_shouldNotBeEqual() throws Exception {
        assertThat(indirectTaxesAge18The90s).isNotEqualTo(indirectTaxesAge19The80s);
    }

    @Test
    public void testEquals_twoObjectWithSameValues_shouldBeEqual() {
        Group dupplicate = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "K02000001", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "cpi1dim1G20100", "02.1 Alcoholic beverages"));
        }}, null);

        assertThat(alcoholicBeverages).isEqualTo(dupplicate);
    }

    @Test
    public void testEquals_twoObjectWithDifferentValues_shouldNotBeEqual() {
        assertThat(clothing).isNotEqualTo(alcoholicBeverages);
    }

    @Test
    public void testHashCode_twoEqualObjects_shouldProduceEqualHashcode() {
        Group dupplicate = new Group(new ArrayList<DimensionData>() {{
            add(new DimensionData(DimensionType.GEOGRAPHY, "K02000001", "K02000001"));
            add(new DimensionData(DimensionType.OTHER, "cpi1dim1G20100", "02.1 Alcoholic beverages"));
        }}, null);

        assertThat(alcoholicBeverages).isEqualTo(dupplicate);
    }
}
