package dp.api;

public class Downloads {

    private XlSFile xls;

    public Downloads(final XlSFile xls) {
        this.xls = xls;
    }

    public XlSFile getXls() {
        return xls;
    }

    public void setXls(XlSFile xls) {
        this.xls = xls;
    }
}
