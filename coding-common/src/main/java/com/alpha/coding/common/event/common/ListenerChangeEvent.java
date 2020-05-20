package com.alpha.coding.common.event.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ListenerChangeEvent
 *
 * @version 1.0
 * Date: 2020-02-20
 */
@Data
@Accessors(chain = true)
public class ListenerChangeEvent {

    private String listenerName;

}
