package com.alpha.coding.bo.function.impl;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.MDC;

/**
 * MDCClearConsumer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MDCClearConsumer implements Consumer<Map<String, String>> {

    @Override
    public void accept(Map<String, String> map) {
        if (map == null || map.isEmpty() || MDC.getMDCAdapter() == null) {
            return;
        }
        for (String key : map.keySet()) {
            MDC.remove(key);
        }
    }
}
