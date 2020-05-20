package com.alpha.coding.common.compute;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ComputeConstant
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ComputeConstant {

    /**
     * 算子分隔
     */
    String OP_SPLIT = "\\+|-|\\||\\*|\\(|\\)| ";

    @Getter
    @AllArgsConstructor
    enum Operator {
        add("+", "并集"),
        minus("-", "差集"),
        of("|", "条件");

        private String op;
        private String desc;
    }

}
