package com.alpha.coding.bo.function.impl;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

/**
 * MapThreadLocalAdaptorCopyConsumer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MapThreadLocalAdaptorCopyConsumer implements Consumer<Map<String, Object>> {

    @Override
    public void accept(Map<String, Object> map) {
        Optional.ofNullable(map).ifPresent(p -> p.forEach((k, v) -> MapThreadLocalAdaptor.put(k, v)));
    }
}
