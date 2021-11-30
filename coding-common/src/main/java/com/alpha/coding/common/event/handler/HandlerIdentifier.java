package com.alpha.coding.common.event.handler;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.EventIdentifier;

/**
 * HandlerIdentifier
 *
 * @param <E> 事件类别
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface HandlerIdentifier<E extends EnumWithCodeSupplier> extends EventIdentifier {

    /**
     * 获取事件类型
     */
    E getEventType();

}
