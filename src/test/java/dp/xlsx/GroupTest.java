package dp.xlsx;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GroupTest {

    private final String[] headers = new String[]{"V4_0", "Time_codelist", "Time", "Geography_codelist", "Geography", "cpi1dim1aggid", "Aggregate"};
    
    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedValues() throws Exception {

        // Given two groups, the first group alphabetically ordered after the second (BBB vs AAA)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroup_getTitle_OrdersByDimensionNameAlphabetically() throws Exception {

        // Given a group
        Group group = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "UK", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);

        // When getTitle is called
        String title = group.getTitle();

        // Then the value contains the option names ordered alphabetically by their dimension name
        Assertions.assertThat(title).isEqualTo("CPI (overall index)\nUK (K02000001)");
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (AAA vs BBB)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered after the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, headers, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, headers, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered before the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupEquals() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, headers, 1);

        // When compareTo is called
        boolean equals = group1.equals(group2);

        // Then the first group is considered to be ordered before the second
        Assertions.assertThat(equals).isTrue();
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_equalValues() throws Exception {

        // Given two groups with the same values
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, headers, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the groups are considered to have the same order
        Assertions.assertThat(compared).isEqualTo(0);
    }
}
