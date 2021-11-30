package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * XLSWriter
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class XLSWriter {

    private static final Map<Class<? extends CellHandler>, CellHandler> CACHE = Maps.newHashMap();
    private static final Map<Sheet, Map<Class<? extends CellHandler>, CellStyle>> CELLSTYLE_CACHE =
            Maps.newHashMap();

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
     *
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
     *
     * @return 生成的Workbook
     */
    public static <T> Workbook generate(List<T> list, Class<T> clazz, boolean xlsx, boolean withHead)
            throws IllegalArgumentException, IllegalAccessException {
        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        writeSheet(sheet, list, clazz, withHead);
        return wb;
    }

    public static <T> void writeSheet(Sheet sheet, List<T> list, Class<T> clazz, boolean withHead)
            throws IllegalAccessException {
        try {
            Field[] fields = clazz.getDeclaredFields();
            Set<Field> canToCellFields = Sets.newHashSet();
            // 表头
            if (withHead) {
                Row headRow = sheet.createRow(0);
                int i = 0;
                for (Field field : fields) {
                    Label label = field.getAnnotation(Label.class);
                    if (label == null) {
                        continue;
                    }
                    canToCellFields.add(field);
                    String value = label.memo();
                    Cell cell = headRow.createCell(i++);
                    cell.setCellValue(value);
                    field.setAccessible(true);
                    Class<? extends CellHandler> headHandlerClazz = label.headCellHandler();
                    processCell(headHandlerClazz, cell, value, String.class);
                }
            } else {
                for (Field field : fields) {
                    Label label = field.getAnnotation(Label.class);
                    if (label == null) {
                        continue;
                    }
                    field.setAccessible(true);
                    canToCellFields.add(field);
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
                int k = 0;
                for (Field field : fields) {
                    if (!canToCellFields.contains(field)) {
                        continue;
                    }
                    Label label = field.getAnnotation(Label.class);
                    Cell cell = row.createCell(k++);
                    Object value = field.get(t);
                    Class<?> type = field.getType();
                    setValue(cell, value, type);
                    Class<? extends CellHandler> cellHandlerClazz = label.cellHandler();
                    processCell(cellHandlerClazz, cell, value, type);
                }
            }
        } finally {
            try {
                CELLSTYLE_CACHE.remove(sheet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void setValue(Cell cell, Object obj, Class<?> type) {
        if (obj == null) {
            cell.setCellValue("");
            return;
        }
        if (type.equals(String.class)) {
            cell.setCellValue(String.valueOf(obj));
        } else if (type.equals(Integer.class)) {
            cell.setCellValue(((Integer) obj).doubleValue());
        } else if (type.equals(Long.class)) {
            cell.setCellValue(((Long) obj).doubleValue());
        } else if (type.equals(Float.class)) {
            cell.setCellValue(((Float) obj).doubleValue());
        } else if (type.equals(Double.class)) {
            cell.setCellValue((Double) obj);
        } else if (type.equals(Boolean.class)) {
            cell.setCellValue((Boolean) obj);
        } else if (type.equals(Date.class)) {
            cell.setCellValue((Date) obj);
        } else if (type.equals(Calendar.class)) {
            cell.setCellValue((Calendar) obj);
        } else if (type.equals(RichTextString.class)) {
            cell.setCellValue((RichTextString) obj);
        } else if (Number.class.isAssignableFrom(type)) {
            cell.setCellValue(((Number) obj).doubleValue());
        } else {
            cell.setCellValue(obj.toString());
        }
    }

    private static void processCell(Class<? extends CellHandler> handlerClazz, Cell cell, Object obj, Class<?> type) {
        CellHandler cellHandler = CACHE.get(handlerClazz);
        final Sheet sheet = cell.getRow().getSheet();
        final Workbook workbook = sheet.getWorkbook();
        Map<Class<? extends CellHandler>, CellStyle> cellStyleMap = CELLSTYLE_CACHE.get(sheet);
        if (cellStyleMap == null) {
            cellStyleMap = Maps.newHashMap();
        }
        CellStyle cellStyle = cellStyleMap.get(handlerClazz);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            if (cellHandler != null) {
                cellHandler.processCellStyle(cellStyle, workbook);
            }
            cellStyleMap.put(handlerClazz, cellStyle);
            CELLSTYLE_CACHE.put(sheet, cellStyleMap);
        }
        if (cellHandler != null) {
            cellHandler.processCell(cell, obj, type, cellStyle);
        }
    }

}
