package com.alpha.coding.bo.function.impl;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.MDC;

/**
 * MDCCopyConsumer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MDCCopyConsumer implements Consumer<Map<String, String>> {

    private static final MDCCopyConsumer INSTANCE = new MDCCopyConsumer();

    public static MDCCopyConsumer getInstance() {
        return INSTANCE;
    }

    @Override
    public void accept(Map<String, String> map) {
        if (map == null || map.isEmpty() || MDC.getMDCAdapter() == null) {
            return;
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }
    }
}
