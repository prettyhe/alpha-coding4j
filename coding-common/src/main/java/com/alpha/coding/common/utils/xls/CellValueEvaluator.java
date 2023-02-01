package com.alpha.coding.common.utils.xls;

import org.apache.poi.ss.usermodel.Cell;

import com.alpha.coding.bo.base.Tuple;

/**
 * CellValueEvaluator
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public interface CellValueEvaluator {

    int CELL_TYPE_NONE = -1;
    int CELL_TYPE_NUMERIC = 0;
    int CELL_TYPE_STRING = 1;
    int CELL_TYPE_FORMULA = 2;
    int CELL_TYPE_BLANK = 3;
    int CELL_TYPE_BOOLEAN = 4;
    int CELL_TYPE_ERROR = 5;

    /**
     * cell取值
     */
    Tuple<String, Object> evaluateCellValueAsString(Cell cell);

    /**
     * 函数类型值计算
     */
    Object evaluateFormulaCellValue(Cell cell);

}
