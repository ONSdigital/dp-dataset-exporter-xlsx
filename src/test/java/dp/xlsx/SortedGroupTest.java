package dp.xlsx;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SortedGroupTest {

    private final Map<Integer, Integer> positionMapping = new HashMap() {{
        put(0, 1);
        put(1, 0);
    }};

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedValues() throws Exception {


        // Given two groups, the first group alphabetically ordered after the second (BBB vs AAA)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);

        SortedGroup sortedGroup1 = new SortedGroup(group1, positionMapping);
        SortedGroup sortedGroup2 = new SortedGroup(group2, positionMapping);

        // When compareTo is called
        int compared = sortedGroup1.compareTo(sortedGroup2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (AAA vs BBB)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, 1);

        SortedGroup sortedGroup1 = new SortedGroup(group1, positionMapping);
        SortedGroup sortedGroup2 = new SortedGroup(group2, positionMapping);

        // When compareTo is called
        int compared = sortedGroup1.compareTo(sortedGroup2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered after the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, 1);

        SortedGroup sortedGroup1 = new SortedGroup(group1, positionMapping);
        SortedGroup sortedGroup2 = new SortedGroup(group2, positionMapping);

        // When compareTo is called
        int compared = sortedGroup1.compareTo(sortedGroup2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, 1);

        SortedGroup sortedGroup1 = new SortedGroup(group1, positionMapping);
        SortedGroup sortedGroup2 = new SortedGroup(group2, positionMapping);

        // When compareTo is called
        int compared = sortedGroup1.compareTo(sortedGroup2);

        // Then the first group is considered to be ordered before the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_equalValues() throws Exception {

        // Given two groups with the same values
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);

        SortedGroup sortedGroup1 = new SortedGroup(group1, positionMapping);
        SortedGroup sortedGroup2 = new SortedGroup(group2, positionMapping);

        // When compareTo is called
        int compared = sortedGroup1.compareTo(sortedGroup2);

        // Then the groups are considered to have the same order
        Assertions.assertThat(compared).isEqualTo(0);
    }

    @Test
    public void testSortedGroup_getTitleWidth() throws Exception {

        String longestDimensionOption = "The is a very long dimension option name";

        // Given a sorted group
        Group group = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", longestDimensionOption}, 1);
        SortedGroup sortedGroup = new SortedGroup(group, positionMapping);

        // When getTitleWidth is called
        int compared = sortedGroup.getTitleWidth();

        // Then the value returned is the length of the longest dimension option
        Assertions.assertThat(compared).isEqualTo(longestDimensionOption.length());
    }
}
