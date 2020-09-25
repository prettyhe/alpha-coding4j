package com.alpha.coding.common.bean.register;

import java.util.EventObject;

/**
 * EnvironmentChangeEvent
 *
 * @version 1.0
 * Date: 2020/9/15
 */
public class EnvironmentChangeEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EnvironmentChangeEvent(Object source) {
        super(source);
    }
}
