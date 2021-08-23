package dp.xlsx;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;

import static dp.logging.LogEvent.info;

/**
 * CMDWorkbook provides a wrapper around {@link SXSSFWorkbook} - calling {@link CMDWorkbook#close()} first invokes
 * {@link SXSSFWorkbook#dispose()} this enables you to use it in a try-with-resources and have the workbook
 * automatically closed.
 */
public class CMDWorkbook extends SXSSFWorkbook {

    public CMDWorkbook() {
        super();
    }

    public CMDWorkbook(XSSFWorkbook workbook) {
        super(workbook);
    }

    public CMDWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize) {
        super(workbook, rowAccessWindowSize);
    }

    public CMDWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize, boolean compressTmpFiles) {
        super(workbook, rowAccessWindowSize, compressTmpFiles);
    }

    public CMDWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize, boolean compressTmpFiles, boolean useSharedStringsTable) {
        super(workbook, rowAccessWindowSize, compressTmpFiles, useSharedStringsTable);
    }

    public CMDWorkbook(int rowAccessWindowSize) {
        super(rowAccessWindowSize);
    }

    @Override
    public void close() throws IOException {
        if (super.dispose()) {
            info().log(this.getClass().getSimpleName() + ".dispose completed successfully");
        }
        info().log("attempting to close CMDWorkbook");
        super.close();
        info().log("CMDWorkbook closed successfully");
    }
}
