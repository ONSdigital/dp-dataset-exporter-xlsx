package dp.api.filter;

/**
 * The downloads section of a filter.
 */
public class Downloads {

    private XLSXFile xls;

    public Downloads(final XLSXFile xls) {
        this.xls = xls;
    }

    public XLSXFile getXls() {
        return xls;
    }

    public void setXls(XLSXFile xls) {
        this.xls = xls;
    }
}
