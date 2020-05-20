package com.alpha.coding.common.utils.xls;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * CellHandler
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface CellHandler {

    /**
     * 对Cell的回调处理，例如设定样式等
     *
     * @param cell      指定的cell
     * @param cellValue 指定cell的value值
     * @param type      指定cell的value值的类型
     * @param cellStyle cell样式
     */
    void processCell(Cell cell, Object cellValue, Class<?> type, CellStyle cellStyle);

    /**
     * 对cellStyle的回调处理
     *
     * @param cellStyle cell样式
     * @param wb        工作簿
     */
    void processCellStyle(CellStyle cellStyle, Workbook wb);

}
