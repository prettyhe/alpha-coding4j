package com.alpha.coding.common.process;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * ExceptionHandler
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ExceptionHandler {

    void handle(AbstractApplicationContext ctx, Throwable throwable);

}
