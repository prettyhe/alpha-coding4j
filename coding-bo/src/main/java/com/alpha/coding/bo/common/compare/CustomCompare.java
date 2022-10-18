package com.alpha.coding.bo.common.compare;

import java.util.function.Function;

import com.alpha.coding.bo.base.Tuple;

/**
 * CustomCompare
 * <p>
 * 输入参数作为元组tuple(input,benchmark)传递给函数function调用,输出CompareResult
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class CustomCompare implements ConditionCompare {

    @Override
    public int type() {
        return ConditionType.CUSTOM.getType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CompareResult compare(Object input, Object benchmark, Function function) {
        return (CompareResult) function.apply(new Tuple<>(input, benchmark));
    }
}
