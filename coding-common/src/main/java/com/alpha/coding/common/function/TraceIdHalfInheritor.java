package com.alpha.coding.common.function;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.alpha.coding.bo.constant.Keys;

/**
 * TraceIdHalfInheritor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class TraceIdHalfInheritor implements Consumer<Map<String, String>> {

    private static final TraceIdHalfInheritor INSTANCE = new TraceIdHalfInheritor();

    public static TraceIdHalfInheritor getInstance() {
        return INSTANCE;
    }

    @Override
    public void accept(Map<String, String> map) {
        if (map == null || map.get(Keys.TRACE_ID) == null) {
            return;
        }
        final String traceId = map.get(Keys.TRACE_ID);
        if (traceId.length() == 0) {
            return;
        }
        final int half = traceId.length() / 2;
        final String newTrace = traceId.substring(0, half) + "-" + genString(traceId.length() - 1 - half);
        map.put(Keys.TRACE_ID, newTrace);
    }

    private String genString(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
        }
        return sb.substring(0, length);
    }
}
