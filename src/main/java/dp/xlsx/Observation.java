package dp.xlsx;

import java.util.Arrays;

public class Observation {

    private final String value;

    private final String[] additionalValues;

    Observation(String value, String[] additionalValues) {
        this.value = value;
        this.additionalValues = additionalValues;
    }

    public String getValue() {
        return value;
    }

    public String[] getAdditionalValues() {
        return additionalValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Observation that = (Observation) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(additionalValues, that.additionalValues);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(additionalValues);
        return result;
    }
}
