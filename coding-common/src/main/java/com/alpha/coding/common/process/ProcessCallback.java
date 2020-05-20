package com.alpha.coding.common.process;

import org.springframework.context.support.AbstractApplicationContext;

/**
 * ProcessCallback
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface ProcessCallback {

    void call(AbstractApplicationContext ctx);

}
