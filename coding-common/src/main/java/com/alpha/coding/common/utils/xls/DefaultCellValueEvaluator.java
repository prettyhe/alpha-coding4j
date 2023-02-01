package com.alpha.coding.common.utils.xls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

import com.alpha.coding.bo.base.Tuple;

import lombok.extern.slf4j.Slf4j;

/**
 * DefaultCellValueEvaluator
 *
 * @version 1.0
 * Date: 2022/3/22
 */
@Slf4j
public class DefaultCellValueEvaluator implements CellValueEvaluator {

    private static final DefaultCellValueEvaluator INSTANCE = new DefaultCellValueEvaluator();
    private static Method METHOD_GET_CELL_TYPE = null;
    private static volatile Method METHOD_CELL_TYPE_GET_CODE = null;

    static {
        try {
            METHOD_GET_CELL_TYPE = Cell.class.getDeclaredMethod("getCellType");
        } catch (NoSuchMethodException e) {
            log.warn("NoSuchMethod: org.apache.poi.ss.usermodel.Cell.getCellType()");
        }
    }

    public static DefaultCellValueEvaluator getDefault() {
        return INSTANCE;
    }

    /**
     * 获取Cell的类型
     */
    public static int getCellTypeInt(Cell cell) {
        if (METHOD_GET_CELL_TYPE == null) {
            return CELL_TYPE_NONE;
        }
        Object ret = null;
        try {
            ret = METHOD_GET_CELL_TYPE.invoke(cell);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("invoke org.apache.poi.ss.usermodel.Cell.getCellType() fail", e);
            return CELL_TYPE_NONE;
        }
        if (ret == null) {
            return CELL_TYPE_NONE;
        }
        if (ret instanceof Integer) {
            return (int) ret;
        }
        if (METHOD_CELL_TYPE_GET_CODE == null) {
            synchronized(DefaultCellValueEvaluator.class) {
                if (METHOD_CELL_TYPE_GET_CODE == null) {
                    try {
                        METHOD_CELL_TYPE_GET_CODE = ret.getClass().getDeclaredMethod("getCode");
                    } catch (NoSuchMethodException e) {
                        log.warn("NoSuchMethod: CellType.getCode()");
                    }
                }
            }
        }
        if (METHOD_CELL_TYPE_GET_CODE != null) {
            try {
                return (int) METHOD_CELL_TYPE_GET_CODE.invoke(ret);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.warn("invoke CellType.getCode() fail from class={},value={}", ret.getClass().getName(), ret, e);
                return CELL_TYPE_NONE;
            }
        }
        return CELL_TYPE_NONE;
    }

    @Override
    public Tuple<String, Object> evaluateCellValueAsString(Cell cell) {
        switch (getCellTypeInt(cell)) {
            case CELL_TYPE_STRING:
                return new Tuple<>(cell.getStringCellValue(), null);
            case CELL_TYPE_NUMERIC:
                BigDecimal bigDecimal = new BigDecimal(Double.toString(cell.getNumericCellValue()));
                String value = bigDecimal.toPlainString().replaceAll("[.][0]*$", "");
                try {
                    return Tuple.of(value, cell.getDateCellValue());
                } catch (Exception e) {
                    return Tuple.of(value, null);
                }
            case CELL_TYPE_BLANK:
                return Tuple.of("", null);
            case CELL_TYPE_BOOLEAN:
                return Tuple.of(String.valueOf(cell.getBooleanCellValue()), null);
            case CELL_TYPE_FORMULA:
                final Object cellValue = evaluateFormulaCellValue(cell);
                return Tuple.of(cellValue == null ? null : String.valueOf(cellValue), null);
            default:
                return Tuple.empty();
        }
    }

    @Override
    public Object evaluateFormulaCellValue(Cell cell) {
        final Workbook wb = cell.getRow().getSheet().getWorkbook();
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        CellValue cellValue = evaluator.evaluate(cell);
        switch (getCellTypeInt(cell)) {
            case CELL_TYPE_BOOLEAN:
                return cellValue.getBooleanValue();
            case CELL_TYPE_NUMERIC:
                return cellValue.getNumberValue();
            case CELL_TYPE_STRING:
                return cellValue.getStringValue();
            case CELL_TYPE_BLANK:
                return "";
            case CELL_TYPE_ERROR:
                return null;
            // CELL_TYPE_FORMULA will never happen
            case CELL_TYPE_FORMULA:
                return null;
            default:
                return null;
        }
    }

}
