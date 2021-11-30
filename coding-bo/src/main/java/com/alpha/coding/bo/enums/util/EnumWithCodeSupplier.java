package com.alpha.coding.bo.enums.util;

import java.util.function.Supplier;

/**
 * EnumWithCodeSupplier
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EnumWithCodeSupplier extends DescSupplier {

    Supplier codeSupply();

}
