package com.alpha.coding.common.utils.xls;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * XLSUtils
 *
 * @version 1.0
 * Date: 2020-02-21
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
     *
     * @return 生成的Workbook
     */
    public static Workbook generate(Object[] headers, List<Object[]> lines, boolean xlsx)
            throws IllegalArgumentException, IllegalAccessException {
        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
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
                setValue(cell, headValue);
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
                setValue(cell, lineValue);
            }
        }
    }

    private static void setValue(Cell cell, Object obj) {
        if (obj == null) {
            cell.setCellValue("");
            return;
        }
        if (obj instanceof String) {
            cell.setCellValue(String.valueOf(obj));
        } else if (obj instanceof Integer) {
            cell.setCellValue(((Integer) obj).doubleValue());
        } else if (obj instanceof Long) {
            cell.setCellValue(((Long) obj).doubleValue());
        } else if (obj instanceof Float) {
            cell.setCellValue(((Float) obj).doubleValue());
        } else if (obj instanceof Double) {
            cell.setCellValue((Double) obj);
        } else if (obj instanceof Boolean) {
            cell.setCellValue((Boolean) obj);
        } else if (obj instanceof Date) {
            cell.setCellValue((Date) obj);
        } else if (obj instanceof Calendar) {
            cell.setCellValue((Calendar) obj);
        } else if (obj instanceof RichTextString) {
            cell.setCellValue((RichTextString) obj);
        } else if (obj instanceof Number) {
            cell.setCellValue(((Number) obj).doubleValue());
        } else {
            cell.setCellValue(obj.toString());
        }
    }

}
