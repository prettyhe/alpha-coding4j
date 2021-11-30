package com.alpha.coding.bo.common.compare;

import java.util.function.Supplier;

import com.alpha.coding.bo.enums.util.CodeSupplyEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ConditionType
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum ConditionType implements CodeSupplyEnum<ConditionType> {

    CUSTOM(0, "自定义", "自定义类型"),
    EXIST(1, "存在", "基准类型不限"),
    VALUE(2, "值", "基准类型是一般类型"),
    NON_VALUE(3, "非值", "基准类型是一般类型"),
    RANGE(4, "范围[,]", "基准类型是范围(ComparableRange)类型"),
    LEFT_OPEN_RANGE(5, "范围(,]", "基准类型是范围(ComparableRange)类型"),
    RIGHT_OPEN_RANGE(6, "范围[,)", "基准类型是范围(ComparableRange)类型"),
    BOTH_OPEN_RANGE(7, "范围(,)", "基准类型是范围(ComparableRange)类型"),
    INCLUDE(8, "包含", "基准类型是集合类型"),
    EXCLUDE(9, "排除", "基准类型是集合类型"),
    REGEXP(10, "正则", "基准类型是正则表达式");

    private final int type;
    private final String name;
    private final String desc;

    @Override
    public Supplier codeSupply() {
        return this::getType;
    }

    @Override
    public Supplier<String> descSupply() {
        return this::getName;
    }

    public static ConditionType valueOf(int type) {
        return CodeSupplyEnum.valueOf(type);
    }

    public static String getDescByCode(int type) {
        return CodeSupplyEnum.getDescByCode(type);
    }

}
