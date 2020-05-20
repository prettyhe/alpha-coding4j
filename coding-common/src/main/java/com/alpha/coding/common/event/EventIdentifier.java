/**
 * Copyright
 */
package com.alpha.coding.common.event;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

/**
 * EventIdentifier 事件标识(事件大类)
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventIdentifier {

    /**
     * 身份标识
     */
    Class<? extends EnumWithCodeSupplier> getIdentity();

}
