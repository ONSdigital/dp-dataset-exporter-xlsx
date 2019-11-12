package dp.api.dataset;

/**
 * Object to encapsulate details about a {@link org.apache.poi.ss.usermodel.Workbook}
 */
public class WorkbookDetails {

    private String downloadURI;
    private long contentLength;

    /**
     * Create new Workbook details.
     *
     * @param downloadURI    the S3 URL to the download file.
     * @param contentLength the size of the download file.
     */
    public WorkbookDetails(String downloadURI, long contentLength) {
        this.downloadURI = downloadURI;
        this.contentLength = contentLength;
    }

    /**
     * @return the S3 URL to the download file.
     */
    public String getDownloadURI() {
        return downloadURI;
    }

    public void setDownloadURI(String downloadURI) {
        this.downloadURI = downloadURI;
    }

    /**
     * @return the size of the download file.
     */
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
                .append(getDownloadURI(), that.getDownloadURI())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(getDownloadURI())
                .append(getContentLength())
                .toHashCode();
    }
}
