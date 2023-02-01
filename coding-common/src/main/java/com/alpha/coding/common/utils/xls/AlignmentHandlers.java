package com.alpha.coding.common.utils.xls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import com.alpha.coding.bo.handler.XLSCellHandler;

/**
 * AlignmentHandlers
 *
 * @version 1.0
 * Date: 2023/1/29
 */
public interface AlignmentHandlers {

    /**
     * AlignmentCenterHandler 水平居中，垂直居中
     *
     * @version 1.0
     * Date: 2023/1/29
     */
    class AlignmentCenterHandler implements XLSCellHandler {

        @Override
        public void processCell(Object cell, Object cellValue, Class<?> type, Object cellStyle) {
            ((Cell) cell).setCellStyle(((CellStyle) cellStyle));
        }

        @Override
        public void processCellStyle(Object cellStyle, Object workbook) {
            ((CellStyle) cellStyle).setAlignment(HorizontalAlignment.CENTER);
            ((CellStyle) cellStyle).setVerticalAlignment(VerticalAlignment.CENTER);
        }
    }

    /**
     * AlignmentLeftHandler 水平左对齐，垂直居中
     *
     * @version 1.0
     * Date: 2023/1/29
     */
    class AlignmentLeftHandler implements XLSCellHandler {

        @Override
        public void processCell(Object cell, Object cellValue, Class<?> type, Object cellStyle) {
            ((Cell) cell).setCellStyle(((CellStyle) cellStyle));
        }

        @Override
        public void processCellStyle(Object cellStyle, Object workbook) {
            ((CellStyle) cellStyle).setAlignment(HorizontalAlignment.LEFT);
            ((CellStyle) cellStyle).setVerticalAlignment(VerticalAlignment.CENTER);
        }
    }

    /**
     * AlignmentRightHandler 水平右对齐，垂直居中
     *
     * @version 1.0
     * Date: 2023/1/29
     */
    class AlignmentRightHandler implements XLSCellHandler {

        @Override
        public void processCell(Object cell, Object cellValue, Class<?> type, Object cellStyle) {
            ((Cell) cell).setCellStyle(((CellStyle) cellStyle));
        }

        @Override
        public void processCellStyle(Object cellStyle, Object workbook) {
            ((CellStyle) cellStyle).setAlignment(HorizontalAlignment.RIGHT);
            ((CellStyle) cellStyle).setVerticalAlignment(VerticalAlignment.CENTER);
        }
    }

    /**
     * AlignmentJustifyHandler 水平两端对齐，垂直居中
     *
     * @version 1.0
     * Date: 2023/1/29
     */
    class AlignmentJustifyHandler implements XLSCellHandler {

        @Override
        public void processCell(Object cell, Object cellValue, Class<?> type, Object cellStyle) {
            ((Cell) cell).setCellStyle(((CellStyle) cellStyle));
        }

        @Override
        public void processCellStyle(Object cellStyle, Object workbook) {
            ((CellStyle) cellStyle).setAlignment(HorizontalAlignment.JUSTIFY);
            ((CellStyle) cellStyle).setVerticalAlignment(VerticalAlignment.CENTER);
        }
    }

}
