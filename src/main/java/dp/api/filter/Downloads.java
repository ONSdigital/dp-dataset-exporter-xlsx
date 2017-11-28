package dp.api.filter;

/**
 * The downloads section of a filter.
 */
public class Downloads {

    private XLSFile xls;

    public Downloads(final XLSFile xls) {
        this.xls = xls;
    }

    public XLSFile getXls() {
        return xls;
    }

    public void setXls(XLSFile xls) {
        this.xls = xls;
    }
}
