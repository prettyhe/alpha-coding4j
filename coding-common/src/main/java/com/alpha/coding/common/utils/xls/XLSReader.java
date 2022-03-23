package com.alpha.coding.common.utils.xls;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.utils.FieldUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * XLSReader
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class XLSReader extends XLSOperator {

    /**
     * 将excel的sheet解析成objects（一行对应一个object）
     *
     * @param sheet     excel sheet
     * @param clazz     object类型
     * @param rowOffset 行偏移量，即去掉头部几行
     * @return 解析结果
     */
    public static <T> List<T> parse(Sheet sheet, Class<T> clazz, int rowOffset) {
        return parse(sheet, clazz, rowOffset, DefaultCellValueEvaluator.getDefault());
    }

    /**
     * 将excel的sheet解析成objects（一行对应一个object）
     *
     * @param sheet     excel sheet
     * @param clazz     object类型
     * @param rowOffset 行偏移量，即去掉头部几行
     * @return 解析结果
     */
    public static <T> List<T> parse(Sheet sheet, Class<T> clazz, int rowOffset,
                                    CellValueEvaluator cellValueEvaluator) {
        CellValueEvaluator evaluator = cellValueEvaluator;
        if (cellValueEvaluator == null) {
            evaluator = DefaultCellValueEvaluator.getDefault();
        }
        List<List<Tuple<String, Object>>> lines = new ArrayList<>();
        int i = 0;
        for (Row row : sheet) {
            if (i < rowOffset) {
                i++;
                continue;
            }
            List<Tuple<String, Object>> line = new ArrayList<>();
            short lastCellNum = row.getLastCellNum();
            for (int j = 0; j < lastCellNum; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    line.add(Tuple.empty());
                    continue;
                }
                line.add(evaluator.evaluateCellValueAsString(cell));
            }
            lines.add(line);
        }
        return makeInstances(lines, clazz);
    }

    private static <T> List<T> makeInstances(List<List<Tuple<String, Object>>> lines, Class<T> clazz) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }
        List<T> ret = new ArrayList<>();
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
                    final Tuple<String, Object> tuple = fieldValues.get(label.order());
                    try {
                        if (label.javaType() == void.class) {
                            value = tuple.getF();
                        } else if (Date.class.isAssignableFrom(label.javaType())) {
                            value = tuple.getS();
                            if (value != null && !(value instanceof Date)) {
                                value = ConvertUtils.convert(value, label.javaType());
                            }
                        } else {
                            value = tuple.getF();
                            if (value != null) {
                                value = ConvertUtils.convert(value, label.javaType());
                            }
                        }
                        if (value != null) {
                            value = ConvertUtils.convert(value, field.getType());
                        }
                    } catch (Exception e) {
                        log.warn("convert value to field fail, fileName={}, valueTuple={}, msg={}",
                                field.getName(), tuple, e.getMessage());
                        throw e;
                    }
                    FieldUtils.setField(newInstance, field.getName(), value);
                }
            }
            return newInstance;
        } catch (Exception e) {
            log.warn("genInstance fail: clazz={}", clazz.getSimpleName(), e);
        }
        return newInstance;
    }

    public static Object getFormulaCellValue(Cell cell) {
        return DefaultCellValueEvaluator.getDefault().evaluateFormulaCellValue(cell);
    }

}
