package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.utils.FieldUtils;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * XLSReader
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class XLSReader {

    /**
     * 设置class类型的convert
     */
    static {
        ConvertUtils.deregister(Long.class);
        ConvertUtils.deregister(Integer.class);

        ConvertUtils.register(new Converter() {
            @Override
            public Long convert(Class type, Object value) {
                if (value == null) {
                    return null;
                }
                String strVal = String.valueOf(value);
                if (StringUtils.isNotBlank(strVal)) {
                    return Double.valueOf(strVal.trim()).longValue();
                }
                return null;
            }
        }, Long.class);

        ConvertUtils.register(new Converter() {
            @Override
            public Integer convert(Class type, Object value) {
                if (value == null) {
                    return null;
                }
                String strVal = String.valueOf(value);
                if (StringUtils.isNotBlank(strVal)) {
                    return Double.valueOf(strVal.trim()).intValue();
                }
                return null;
            }
        }, Integer.class);

        ConvertUtils.register(new Converter() {
            @Override
            public BigDecimal convert(Class type, Object value) {
                if (value == null) {
                    return null;
                }
                String strVal = String.valueOf(value);
                if (StringUtils.isNotBlank(strVal)) {
                    return new BigDecimal(strVal.trim());
                }
                return null;
            }
        }, BigDecimal.class);
    }

    /**
     * 将excel的sheet解析成objects（一行对应一个object）
     *
     * @param sheet     excel sheet
     * @param clazz     object类型
     * @param rowOffset 行偏移量，即去掉头部几行
     * @return 解析结果
     */
    public static <T> List<T> parse(Sheet sheet, Class<T> clazz, int rowOffset) {
        List<List<Tuple<String, Object>>> lines = Lists.newArrayList();
        int i = 0;
        for (Row row : sheet) {
            if (i < rowOffset) {
                i++;
                continue;
            }
            List<Tuple<String, Object>> line = Lists.newArrayList();
            short lastCellNum = row.getLastCellNum();
            for (int j = 0; j < lastCellNum; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    Tuple<String, Object> tuple = new Tuple<>();
                    line.add(tuple);
                    continue;
                }
                Tuple<String, Object> cellStringValue = getCellValueAsString(cell);
                line.add(cellStringValue);
            }
            lines.add(line);
        }
        return makeInstances(lines, clazz);
    }

    @SuppressWarnings("deprecation")
    private static Tuple<String, Object> getCellValueAsString(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return new Tuple<>(cell.getStringCellValue(), null);
            case NUMERIC:
                BigDecimal bigDecimal = new BigDecimal(Double.toString(cell.getNumericCellValue()));
                String value = bigDecimal.toPlainString().replaceAll("[.][0]{0,}$", "");
                try {
                    return new Tuple<String, Object>(value, cell.getDateCellValue());
                } catch (Exception e) {
                    return new Tuple<>(value, null);
                }
            case BOOLEAN:
                return new Tuple<>(String.valueOf(cell.getBooleanCellValue()), null);
            case BLANK:
                return new Tuple<>();
            case FORMULA:
                final Object cellValue = getFormulaCellValue(cell);
                return new Tuple<>(cellValue == null ? null : String.valueOf(cellValue), null);
            default:
                return new Tuple<>();
        }
    }

    private static <T> List<T> makeInstances(List<List<Tuple<String, Object>>> lines, Class<T> clazz) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        List<T> ret = Lists.newArrayList();
        for (List<Tuple<String, Object>> fieldValues : lines) {
            T t = genInstance(fieldValues, clazz);
            if (t == null) {
                continue;
            }
            ret.add(t);
        }
        return ret;
    }

    private static <T> T genInstance(List<Tuple<String, Object>> fieldValues, Class<T> clazz) {
        T newInstance = null;
        try {
            newInstance = clazz.newInstance();
            List<Field> matchedFields = FieldUtils.findMatchedFields(clazz, Label.class);
            for (Field field : matchedFields) {
                Label label = field.getAnnotation(Label.class);
                if (label.order() < fieldValues.size()) {
                    Object value = null;
                    if (label.javaType().equals(Date.class)) {
                        value = fieldValues.get(label.order()).getS();
                    } else {
                        value = ConvertUtils.convert(fieldValues.get(label.order()).getF(), label.javaType());
                    }
                    FieldUtils.setField(newInstance, field.getName(), value);
                }
            }
            return newInstance;
        } catch (Exception e) {
            log.warn("genInstance fail: fieldValues={}, clazz={}", fieldValues, clazz.getSimpleName(), e);
        }
        return newInstance;
    }

    public static Object getFormulaCellValue(Cell cell) {
        final Workbook wb = cell.getRow().getSheet().getWorkbook();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        CellValue cellValue = evaluator.evaluate(cell);
        switch (cellValue.getCellTypeEnum()) {
            case BOOLEAN:
                return cellValue.getBooleanValue();
            case NUMERIC:
                return cellValue.getNumberValue();
            case STRING:
                return cellValue.getStringValue();
            case BLANK:
                return "";
            case ERROR:
                return null;
            // CELL_TYPE_FORMULA will never happen
            case FORMULA:
                return null;
        }
        return null;
    }

}
