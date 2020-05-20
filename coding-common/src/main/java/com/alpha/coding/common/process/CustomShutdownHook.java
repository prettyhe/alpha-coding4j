package com.alpha.coding.common.process;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * CustomShutdownHook
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface CustomShutdownHook {

    Runnable[] genShutdownHooks(AbstractApplicationContext applicationContext);

}
