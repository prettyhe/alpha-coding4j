package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alpha.coding.common.utils.FieldUtils;

/**
 * XLSWriter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class XLSWriter {

    private static final Map<Class<? extends CellHandler>, CellHandler> CACHE = new HashMap<>();
    private static final Map<Sheet, Map<Class<? extends CellHandler>, CellStyle>> CELL_STYLE_CACHE =
            new ConcurrentHashMap<>();

    /**
     * 添加cell处理
     *
     * @param cellHandler cell处理回调
     */
    public static void registerCellHandler(CellHandler cellHandler) {
        CACHE.put(cellHandler.getClass(), cellHandler);
    }

    /**
     * excel Workbook生成
     *
     * @param list  对象list
     * @param clazz 对象类型
     * @param xlsx  是否生成xlsx格式
     * @return 生成的Workbook
     */
    public static <T> Workbook generate(List<T> list, Class<T> clazz, boolean xlsx)
            throws IllegalArgumentException, IllegalAccessException {
        return generate(list, clazz, xlsx, true);
    }

    /**
     * excel Workbook生成
     *
     * @param list     对象list
     * @param clazz    对象类型
     * @param xlsx     是否生成xlsx格式
     * @param withHead 是否包含头部
     * @return 生成的Workbook
     */
    public static <T> Workbook generate(List<T> list, Class<T> clazz, boolean xlsx, boolean withHead)
            throws IllegalArgumentException, IllegalAccessException {
        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        writeSheet(sheet, list, clazz, withHead);
        return wb;
    }

    /**
     * 写入内容到表格
     *
     * @param sheet    表格
     * @param list     内容对象
     * @param clazz    表对应的类型
     * @param withHead 是否需要添加表头
     */
    public static <T> void writeSheet(Sheet sheet, List<T> list, Class<T> clazz, boolean withHead)
            throws IllegalAccessException {
        try {
            List<Field> fields = FieldUtils.findMatchedFields(clazz, Label.class);
            final TreeMap<Integer, Field> canToCellFieldMap = new TreeMap<>();
            final Map<Field, Label> fieldLabelMap = new HashMap<>();
            for (Field field : fields) {
                Label label = field.getAnnotation(Label.class);
                field.setAccessible(true);
                canToCellFieldMap.put(label.order(), field);
                fieldLabelMap.put(field, label);
            }
            // 表头
            if (withHead) {
                Row headRow = sheet.createRow(0);
                for (Field field : canToCellFieldMap.values()) {
                    Label label = fieldLabelMap.get(field);
                    String value = label.memo();
                    Cell cell = headRow.createCell(label.order());
                    cell.setCellValue(value);
                    Class<? extends CellHandler> headHandlerClazz = label.headCellHandler();
                    processCell(headHandlerClazz, cell, value, String.class);
                }
            }
            // 表内容
            if (list == null) {
                return;
            }
            int rowOffset = withHead ? 1 : 0;
            for (int j = 0; j < list.size(); j++) {
                T t = list.get(j);
                Row row = sheet.createRow(j + rowOffset);
                for (Field field : canToCellFieldMap.values()) {
                    Label label = fieldLabelMap.get(field);
                    Cell cell = row.createCell(label.order());
                    Object value = field.get(t);
                    Class<?> type = field.getType();
                    if (label.javaType() != void.class) {
                        value = ConvertUtils.convert(value, label.javaType());
                    }
                    setValue(cell, value);
                    Class<? extends CellHandler> cellHandlerClazz = label.cellHandler();
                    processCell(cellHandlerClazz, cell, value, type);
                }
            }
        } finally {
            try {
                CELL_STYLE_CACHE.remove(sheet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setValue(Cell cell, Object obj) {
        if (obj == null) {
            cell.setCellValue("");
            return;
        }
        if (obj instanceof String) {
            cell.setCellValue(String.valueOf(obj));
        } else if (obj instanceof Number) {
            cell.setCellValue(((Number) obj).doubleValue());
        } else if (obj instanceof Boolean) {
            cell.setCellValue((Boolean) obj);
        } else if (obj instanceof Date) {
            cell.setCellValue((Date) obj);
        } else if (obj instanceof Calendar) {
            cell.setCellValue((Calendar) obj);
        } else if (obj instanceof RichTextString) {
            cell.setCellValue((RichTextString) obj);
        } else {
            cell.setCellValue(obj.toString());
        }
    }

    private static void processCell(Class<? extends CellHandler> handlerClazz, Cell cell, Object obj, Class<?> type) {
        CellHandler cellHandler = CACHE.get(handlerClazz);
        final Sheet sheet = cell.getRow().getSheet();
        final Workbook workbook = sheet.getWorkbook();
        Map<Class<? extends CellHandler>, CellStyle> cellStyleMap = CELL_STYLE_CACHE.get(sheet);
        if (cellStyleMap == null) {
            cellStyleMap = new HashMap<>();
        }
        CellStyle cellStyle = cellStyleMap.get(handlerClazz);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            if (cellHandler != null) {
                cellHandler.processCellStyle(cellStyle, workbook);
            }
            cellStyleMap.put(handlerClazz, cellStyle);
            CELL_STYLE_CACHE.put(sheet, cellStyleMap);
        }
        if (cellHandler != null) {
            cellHandler.processCell(cell, obj, type, cellStyle);
        }
    }

}
