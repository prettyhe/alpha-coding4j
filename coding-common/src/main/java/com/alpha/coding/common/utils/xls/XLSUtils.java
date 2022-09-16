package com.alpha.coding.common.utils.xls;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * XLSUtils
 *
 * @version 1.0
 */
public class XLSUtils {

    public static Workbook createWorkbook(boolean xlsx) {
        return xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
    }

    /**
     * excel Workbook生成
     *
     * @param headers 头部
     * @param lines   行内容
     * @param xlsx    是否生成xlsx格式
     * @return 生成的Workbook
     */
    public static Workbook generate(Object[] headers, List<Object[]> lines, boolean xlsx)
            throws IllegalArgumentException {
        return generate(headers, lines, xlsx, null);
    }

    /**
     * excel Workbook生成
     *
     * @param headers   头部
     * @param lines     行内容
     * @param xlsx      是否生成xlsx格式
     * @param sheetName sheet名
     * @return 生成的Workbook
     */
    public static Workbook generate(Object[] headers, List<Object[]> lines, boolean xlsx, String sheetName)
            throws IllegalArgumentException {
        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = sheetName != null ? wb.createSheet(sheetName) : wb.createSheet();
        writeSheet(sheet, headers, lines);
        return wb;
    }

    /**
     * 写入到excel的sheet
     *
     * @param sheet   sheet
     * @param headers 头
     * @param lines   行
     */
    public static void writeSheet(Sheet sheet, Object[] headers, List<Object[]> lines) {
        boolean withHead = false;
        // 表头
        if (headers != null && headers.length > 0) {
            withHead = true;
            Row headRow = sheet.createRow(0);
            int i = 0;
            for (Object headValue : headers) {
                Cell cell = headRow.createCell(i++);
                XLSWriter.setValue(cell, headValue);
            }
        }
        // 表内容
        if (lines == null) {
            return;
        }
        int rowOffset = withHead ? 1 : 0;
        for (int j = 0; j < lines.size(); j++) {
            Object[] line = lines.get(j);
            Row row = sheet.createRow(j + rowOffset);
            int k = 0;
            for (Object lineValue : line) {
                Cell cell = row.createCell(k++);
                XLSWriter.setValue(cell, lineValue);
            }
        }
    }

}
