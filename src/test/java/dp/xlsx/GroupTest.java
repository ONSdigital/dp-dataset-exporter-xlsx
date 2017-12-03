package dp.xlsx;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GroupTest {

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedValues() throws Exception {

        // Given two groups, the first group alphabetically ordered after the second (BBB vs AAA)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (AAA vs BBB)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "BBB", "cpi1dim1A0", "CPI (overall index)"}, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_unsortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered after the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered after the second
        Assertions.assertThat(compared).isGreaterThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_sortedSecondaryValues() throws Exception {

        // Given two groups, the first group alphabetically ordered before the second (DDD vs CCC)
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CCC"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "DDD"}, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the first group is considered to be ordered before the second
        Assertions.assertThat(compared).isLessThan(0);
    }

    @Test
    public void testGroupCompare_ordersByGroupValuesAlphabetally_equalValues() throws Exception {

        // Given two groups with the same values
        Group group1 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);
        Group group2 = new Group(new String[]{"86.8", "Month", "Jan-96", "K02000001", "AAA", "cpi1dim1A0", "CPI (overall index)"}, 1);

        // When compareTo is called
        int compared = group1.compareTo(group2);

        // Then the groups are considered to have the same order
        Assertions.assertThat(compared).isEqualTo(0);
    }
}
