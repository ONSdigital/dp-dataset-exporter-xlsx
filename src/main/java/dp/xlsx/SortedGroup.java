package dp.xlsx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SortedGroup implements Comparable<SortedGroup> {

    private final Group group;
    private final List<String> groupValues; // the unique dimension options

    final Comparator<String> stringLenComparator = Comparator.comparingInt(String::length);

    /**
     * Groups are by default ordered by the original order in the v4 file.
     *
     * @param group           - the original group that needs sorting
     * @param positionMapping - a map with the original position of the header and the ordered position it should be
     *                        when sorted.
     */
    SortedGroup(Group group, Map<Integer, Integer> positionMapping) {
        this.group = group;

        // apply position mappings to the group values

        List<String> unsortedGroupValues = group.getGroupValues();
        groupValues = new ArrayList<>(unsortedGroupValues);

        for (int i = 0; i < unsortedGroupValues.size(); ++i) {

            int sortedPosition = positionMapping.get(i);
            groupValues.set(sortedPosition, unsortedGroupValues.get(i));
        }
    }

    String getTitle() {
        return getGroupValues().stream()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Return the length of the longest title line.
     */
    int getTitleWidth() {

        return getGroupValues().stream()
                .max(stringLenComparator)
                .get().length();
    }

    String getObservation(String time) {
        return this.group.getObservation(time);
    }

    @Override
    public int compareTo(SortedGroup o) {

        int compared = 0;

        // if the group arrays differ in length do not try and compare.
        if (getGroupValues().size() != o.getGroupValues().size())
            return 0;

        // Order by each group value in turn
        for (int i = 0; i < getGroupValues().size(); ++i) {

            compared = getGroupValues().get(i).compareTo(o.getGroupValues().get(i));

            // return if we can determine order from this dimension option
            if (compared != 0)
                return compared;
        }

        return compared;
    }

    List<String> getGroupValues() {
        return groupValues;
    }
}
