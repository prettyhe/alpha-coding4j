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

    /**
     * cell取值
     */
    Tuple<String, Object> evaluateCellValueAsString(Cell cell);

    /**
     * 函数类型值计算
     */
    Object evaluateFormulaCellValue(Cell cell);

}
