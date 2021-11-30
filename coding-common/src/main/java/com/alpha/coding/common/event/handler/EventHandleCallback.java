package com.alpha.coding.common.event.handler;

import com.alpha.coding.bo.enums.util.EnumWithCodeSupplier;

/**
 * EventHandleCallback
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface EventHandleCallback {

    /**
     * 事件处理完后操作
     *
     * @param eventID 事件ID
     * @param type    事件类型
     * @param <E>     事件类别
     */
    <E extends EnumWithCodeSupplier> void afterHandle(String eventID, E type);

}
