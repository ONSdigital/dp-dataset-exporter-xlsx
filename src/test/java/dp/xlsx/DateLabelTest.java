package dp.xlsx;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DateLabelTest {

    @Test
    public void determineFormat_MMM_yy() throws Exception {

        // Given a date label with the format MMM-yy
        String dateString = "Feb-96";

        // When determine is called
        String format = DateLabel.determineDateFormat(dateString);

        // Then the expected format is returned
        Assertions.assertThat(format).isEqualTo("MMM-yy");
    }

    @Test
    public void determineFormat_unrecognised() throws Exception {

        // Given a date label with an unrecognised format
        String dateString = "some-date-format-not-recognised";

        // When determine is called
        String format = DateLabel.determineDateFormat(dateString);

        // Then null is returned
        Assertions.assertThat(format).isNull();
    }
}
