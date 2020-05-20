/**
 * Copyright
 */
package com.alpha.coding.common.event.eventbus;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;
import com.alpha.coding.common.event.AbstractEvent;
import com.alpha.coding.common.event.EventIdentifier;

/**
 * EventBus
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventBus extends EventIdentifier {

    /**
     * 发送事件
     *
     * @param event 事件
     * @param <K>   KEY类型
     * @param <E>   事件类型
     * @param <AE>  事件类
     */
    <K, E extends EnumWithCodeSupplier, AE extends AbstractEvent<K, E>> void post(AE event);

}
