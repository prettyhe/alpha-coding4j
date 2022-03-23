package com.alpha.coding.common.utils.xls;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

import com.alpha.coding.bo.base.Tuple;

/**
 * DefaultCellValueEvaluator
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public class DefaultCellValueEvaluator implements CellValueEvaluator {

    private static final DefaultCellValueEvaluator INSTANCE = new DefaultCellValueEvaluator();

    public static DefaultCellValueEvaluator getDefault() {
        return INSTANCE;
    }

    @Override
    public Tuple<String, Object> evaluateCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return new Tuple<>(cell.getStringCellValue(), null);
            case Cell.CELL_TYPE_NUMERIC:
                BigDecimal bigDecimal = new BigDecimal(Double.toString(cell.getNumericCellValue()));
                String value = bigDecimal.toPlainString().replaceAll("[.][0]*$", "");
                try {
                    return Tuple.of(value, cell.getDateCellValue());
                } catch (Exception e) {
                    return Tuple.of(value, null);
                }
            case Cell.CELL_TYPE_BLANK:
                return Tuple.of("", null);
            case Cell.CELL_TYPE_BOOLEAN:
                return Tuple.of(String.valueOf(cell.getBooleanCellValue()), null);
            case Cell.CELL_TYPE_FORMULA:
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
        switch (cellValue.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cellValue.getBooleanValue();
            case Cell.CELL_TYPE_NUMERIC:
                return cellValue.getNumberValue();
            case Cell.CELL_TYPE_STRING:
                return cellValue.getStringValue();
            case Cell.CELL_TYPE_BLANK:
                return "";
            case Cell.CELL_TYPE_ERROR:
                return null;
            // CELL_TYPE_FORMULA will never happen
            case Cell.CELL_TYPE_FORMULA:
                return null;
            default:
                return null;
        }
    }

}
