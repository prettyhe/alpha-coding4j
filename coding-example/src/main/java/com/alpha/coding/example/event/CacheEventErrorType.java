package com.alpha.coding.example.event;

import java.util.function.Supplier;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CacheEventErrorType
 *
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum CacheEventErrorType implements EnumWithCodeSupplier {
    /**
     * 读取失败，意味着错误的key
     */
    READ_FAIL(1001, "读取失败"),

    /**
     * 计算失败
     */
    CALCULATE_FAIL(1002, "计算失败"),

    /**
     * 写入失败
     */
    WRITE_FAIL(1003, "写入失败");

    private int type;
    private String msg;

    @Override
    public Supplier codeSupply() {
        return () -> this.type;
    }
}
