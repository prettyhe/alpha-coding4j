package com.alpha.coding.bo.handler;

/**
 * XLSCellHandler
 *
 * @version 1.0
 * Date: 2022/8/18
 */
public interface XLSCellHandler {

    /**
     * 对Cell的回调处理，例如设定样式等
     *
     * @param cell      指定的cell(org.apache.poi.ss.usermodel.Cell)
     * @param cellValue 指定cell的value值
     * @param type      指定cell的value值的类型
     * @param cellStyle cell样式(org.apache.poi.ss.usermodel.CellStyle)
     */
    void processCell(Object cell, Object cellValue, Class<?> type, Object cellStyle);

    /**
     * 对cellStyle的回调处理
     *
     * @param cellStyle cell样式(org.apache.poi.ss.usermodel.CellStyle)
     * @param workbook  工作簿(org.apache.poi.ss.usermodel.Workbook)
     */
    void processCellStyle(Object cellStyle, Object workbook);

}
