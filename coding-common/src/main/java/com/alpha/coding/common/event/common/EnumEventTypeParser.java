package com.alpha.coding.common.event.common;

import java.util.function.BiFunction;

import com.alpha.coding.bo.enums.util.EnumUtils;
import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

/**
 * EnumEventTypeParser
 *
 * @version 1.0
 * Date: 2020-02-19
 */
public class EnumEventTypeParser implements
        BiFunction<Class<? extends EnumWithCodeSupplier>, String, EnumWithCodeSupplier> {

    @Override
    public EnumWithCodeSupplier apply(Class<? extends EnumWithCodeSupplier> clz, String str) {
        return EnumUtils.safeParseEnumByName(clz, str);
    }
}
