package com.alpha.coding.example.event;

import java.util.function.Supplier;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CacheEventType
 *
 * @version 1.0
 * Date: 2020-03-21
 */
@Getter
@AllArgsConstructor
public enum CacheEventType implements EnumWithCodeSupplier {

    DATA_A_CHANGE(1, "数据A更新"),
    ;

    private final int type;
    private final String desc;

    @Override
    public Supplier codeSupply() {
        return () -> this.type;
    }

}
