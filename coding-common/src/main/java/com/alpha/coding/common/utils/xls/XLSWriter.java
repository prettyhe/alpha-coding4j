package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.handler.XLSCellHandler;
import com.alpha.coding.common.utils.ClassUtils;
import com.alpha.coding.common.utils.DateUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * XLSWriter
 *
 * @version 1.0
 */
@Slf4j
public class XLSWriter extends XLSOperator {

    private static final Map<Class<? extends XLSCellHandler>, XLSCellHandler> CELL_HANDLER_CACHE =
            new ConcurrentHashMap<>();
    private static final Map<Sheet, Map<Class<? extends XLSCellHandler>, CellStyle>> CELL_STYLE_CACHE =
            new ConcurrentHashMap<>();
    private static final String XLS_CELL_HANDLER_LOCAL_KEY = "XLSCellHandlerMap";

    /**
     * 添加cell处理
     *
     * @param cellHandler cell处理回调
     */
    public static void registerCellHandler(XLSCellHandler cellHandler) {
        CELL_HANDLER_CACHE.put(cellHandler.getClass(), cellHandler);
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
     * @param list      对象list
     * @param clazz     对象类型
     * @param xlsx      是否生成xlsx格式
     * @param sheetName Sheet名称
     * @return 生成的Workbook
     */
    public static <T> Workbook generate(List<T> list, Class<T> clazz, boolean xlsx, String sheetName)
            throws IllegalArgumentException, IllegalAccessException {
        return generate(list, clazz, xlsx, true, sheetName);
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
        return generate(list, clazz, xlsx, withHead, null);
    }

    /**
     * excel Workbook生成
     *
     * @param list      对象list
     * @param clazz     对象类型
     * @param xlsx      是否生成xlsx格式
     * @param withHead  是否包含头部
     * @param sheetName Sheet名称
     * @return 生成的Workbook
     */
    public static <T> Workbook generate(List<T> list, Class<T> clazz, boolean xlsx, boolean withHead, String sheetName)
            throws IllegalArgumentException, IllegalAccessException {
        Workbook wb = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
        Sheet sheet = null;
        if (StringUtils.isNotBlank(sheetName)) {
            sheet = wb.createSheet(sheetName);
        } else {
            sheet = wb.createSheet();
        }
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
            final TreeMap<Integer, Field> canToCellFieldMap = new TreeMap<>();
            final Map<Field, XLSLabelContext> fieldLabelMap = XLSOperator.fieldLabelMap(clazz);
            fieldLabelMap.forEach((k, v) -> {
                k.setAccessible(true);
                canToCellFieldMap.put(v.getOrder(), k);
            });
            // 表头
            if (withHead) {
                Row headRow = sheet.createRow(0);
                for (Field field : canToCellFieldMap.values()) {
                    XLSLabelContext label = fieldLabelMap.get(field);
                    String value = label.getMemo();
                    Cell cell = headRow.createCell(label.getOrder());
                    cell.setCellValue(value);
                    Class<? extends XLSCellHandler>[] headHandlerClazz = label.getHeadCellHandler();
                    for (Class<? extends XLSCellHandler> handlerClazz : headHandlerClazz) {
                        processCell(handlerClazz, cell, value, String.class);
                    }
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
                    XLSLabelContext label = fieldLabelMap.get(field);
                    Cell cell = row.createCell(label.getOrder());
                    Object value = field.get(t);
                    Class<?> type = field.getType();
                    if (label.getJavaType() != void.class) {
                        if (Date.class.isAssignableFrom(type) && label.getJavaType() == String.class) {
                            value = DateUtils.format((Date) value, label.getOutDateFormat());
                            type = String.class;
                        } else {
                            value = ConvertUtils.convert(value, label.getJavaType());
                        }
                    }
                    setValue(cell, value);
                    Class<? extends XLSCellHandler>[] cellHandlerClazz = label.getCellHandler();
                    for (Class<? extends XLSCellHandler> handlerClazz : cellHandlerClazz) {
                        processCell(handlerClazz, cell, value, type);
                    }
                }
            }
        } finally {
            try {
                CELL_STYLE_CACHE.remove(sheet);
            } catch (Exception e) {
                e.printStackTrace();
            }
            MapThreadLocalAdaptor.remove(XLS_CELL_HANDLER_LOCAL_KEY);
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

    @SuppressWarnings({"unckecked"})
    private static void processCell(Class<? extends XLSCellHandler> handlerClass, Cell cell,
                                    Object obj, Class<?> type) {
        XLSCellHandler cellHandler = CELL_HANDLER_CACHE.get(handlerClass);
        if (cellHandler == null && handlerClass != XLSCellHandler.class && handlerClass != CellHandler.class) {
            Map<Class<? extends XLSCellHandler>, XLSCellHandler> handlerMap =
                    MapThreadLocalAdaptor.computeIfAbsent(XLS_CELL_HANDLER_LOCAL_KEY, k -> new HashMap<>());
            cellHandler = handlerMap.computeIfAbsent(handlerClass, c -> {
                try {
                    return ClassUtils.newInstance(c);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                        InvocationTargetException e) {
                    log.warn("create XLSCellHandler fail for {}", handlerClass.getName(), e);
                }
                return null;
            });
        }
        final Sheet sheet = cell.getRow().getSheet();
        final Workbook workbook = sheet.getWorkbook();
        Map<Class<? extends XLSCellHandler>, CellStyle> cellStyleMap = CELL_STYLE_CACHE.get(sheet);
        if (cellStyleMap == null) {
            cellStyleMap = new HashMap<>();
        }
        CellStyle cellStyle = cellStyleMap.get(handlerClass);
        if (cellStyle == null) {
            cellStyle = workbook.createCellStyle();
            if (cellHandler != null) {
                cellHandler.processCellStyle(cellStyle, workbook);
            }
            cellStyleMap.put(handlerClass, cellStyle);
            CELL_STYLE_CACHE.put(sheet, cellStyleMap);
        }
        if (cellHandler != null) {
            cellHandler.processCell(cell, obj, type, cellStyle);
        }
    }

}
