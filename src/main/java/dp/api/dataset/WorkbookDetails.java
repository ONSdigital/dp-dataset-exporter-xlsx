package dp.api.dataset;

public class WorkbookDetails {

    private String dowloadURI;
    private long contentLength;

    public WorkbookDetails(String dowloadURI, long contentLength) {
        this.dowloadURI = dowloadURI;
        this.contentLength = contentLength;
    }

    public String getDowloadURI() {
        return dowloadURI;
    }

    public void setDowloadURI(String dowloadURI) {
        this.dowloadURI = dowloadURI;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WorkbookDetails that = (WorkbookDetails) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(getContentLength(), that.getContentLength())
                .append(getDowloadURI(), that.getDowloadURI())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(getDowloadURI())
                .append(getContentLength())
                .toHashCode();
    }
}
