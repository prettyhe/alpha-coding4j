package com.alpha.coding.bo.page;

import java.util.function.Supplier;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Order
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Getter
@AllArgsConstructor
public enum Order implements EnumWithCodeSupplier {

    ASC("asc"), DESC("desc");

    private final String name;

    @Override
    public Supplier codeSupply() {
        return this::getName;
    }
}
