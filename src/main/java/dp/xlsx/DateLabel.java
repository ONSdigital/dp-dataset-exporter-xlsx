package dp.xlsx;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for working with date labels in v4 files.
 */
public class DateLabel {

    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
        put("^\\w{3}-\\d{2}$", "MMM-yy");
    }};

    /**
     * Take a date label and return it's format if it's recognised.
     */
    public static String determineDateFormat(String dateString) {

        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }
        return null; // Unknown format.
    }
}
