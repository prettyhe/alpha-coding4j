package com.alpha.coding.bo.function.impl;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

/**
 * MapThreadLocalAdaptorClearConsumer
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MapThreadLocalAdaptorClearConsumer implements Consumer<Map<String, Object>> {

    private static final MapThreadLocalAdaptorClearConsumer INSTANCE = new MapThreadLocalAdaptorClearConsumer();

    public static MapThreadLocalAdaptorClearConsumer getInstance() {
        return INSTANCE;
    }

    @Override
    public void accept(Map<String, Object> map) {
        Optional.ofNullable(map).ifPresent(p -> p.keySet().forEach(MapThreadLocalAdaptor::remove));
    }
}
