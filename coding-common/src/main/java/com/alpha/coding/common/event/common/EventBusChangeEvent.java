package com.alpha.coding.common.event.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * EventBusChangeEvent
 *
 * @version 1.0
 * Date: 2020-02-19
 */
@Data
@Accessors(chain = true)
public class EventBusChangeEvent {

    private String eventBusName;

}
