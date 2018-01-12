package dp.xlsx;

// Represents a single entry of data for a dimension / dimension option.
public class DimensionData implements Comparable<DimensionData> {

    private DimensionType dimensionType;
    private String code;
    private String value;

    public DimensionData(DimensionType dimensionType, String label, String code) {
        this.dimensionType = dimensionType;
        this.code = code;

        if ("".equals(label))
            value = code;
        else {
            value = label;
        }
    }

    public DimensionType getDimensionType() {
        return dimensionType;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimensionData that = (DimensionData) o;

        if (dimensionType != that.dimensionType) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = dimensionType != null ? dimensionType.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(DimensionData o) {
        return this.getValue().compareTo(o.getValue());
    }
}
