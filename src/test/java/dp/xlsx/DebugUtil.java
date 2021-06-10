package dp.xlsx;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DebugUtil {

    public static Predicate<Group> dimMatcher(final String code, final String val) {
        return (Group g) -> g.getGroupValues()
                .stream()
                .filter(d -> d.getDimensionType().equals(DimensionType.OTHER) && d.getCode().equals(code) && d.getValue().equals(val))
                .findFirst()
                .isPresent();
    }

    public static List<Group> filterGroups(Collection<Group> input, Predicate<Group> filter) {
        return input
                .stream()
                .filter(g -> filter.test(g))
                .collect(Collectors.toList());
    }

    public static void display(List<Group> groups) {
        System.out.println(groups.size() + " entries");
        groups.stream().forEach(entry -> displayGroup(entry));
    }

    public static void displayGroup(Group g) {
        System.out.println(g.getGroupValues() + " " + g.getObservations()
                .entrySet()
                .stream()
                .map(e -> e.getKey() + " " + e.getValue().getValue())
                .collect(Collectors.toList()));
    }
}
