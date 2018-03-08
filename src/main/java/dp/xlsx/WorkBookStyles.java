package dp.xlsx;

import org.apache.poi.ss.usermodel.*;

class WorkBookStyles {

    private final CellStyle headingStyle;
    private final CellStyle headerRightAlignStyle;
    private final CellStyle valueStyle;
    private final CellStyle valueRightAlignStyle;
    private final CellStyle numberStyle;
    private final CellStyle noteStyle;

    WorkBookStyles(Workbook workbook) {
        headingStyle = createBoldStyle(workbook);
        headerRightAlignStyle = createBoldRightAlignStyle(workbook);
        valueStyle = createStyle(workbook);
        valueRightAlignStyle = createRightAlignStyle(workbook);
        numberStyle = createNumberStyle(workbook);
        noteStyle = createNoteStyle(workbook);
    }

    private CellStyle createStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createRightAlignStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createLinkStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Link");
        font.setFontHeightInPoints((short) 14);
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.TOP);

        return style;
    }

    private CellStyle createBoldStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Bold");
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(false);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createBoldRightAlignStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Bold");
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setWrapText(false);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createNumberStyle(Workbook wb) {
        final CellStyle style = wb.createCellStyle();
        final Font font = wb.createFont();
        font.setFontName("Arial-Number");
        font.setFontHeightInPoints((short) 14);
        style.setDataFormat(wb.createDataFormat().getFormat("0.0############################"));
        style.setFont(font);
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createNoteStyle(Workbook wb) {
        CellStyle style = createStyle(wb);
        style.setWrapText(false);
        return style;
    }

    CellStyle getHeadingStyle() {
        return headingStyle;
    }

    CellStyle getHeaderRightAlignStyle() {
        return headerRightAlignStyle;
    }

    CellStyle getValueStyle() {
        return valueStyle;
    }

    CellStyle getValueRightAlignStyle() {
        return valueRightAlignStyle;
    }


    CellStyle getNumberStyle() {
        return numberStyle;
    }

    CellStyle getNoteStyle() {
        return noteStyle;
    }
}
