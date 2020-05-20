package com.alpha.coding.bo.json;

import java.util.function.Supplier;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.constant.Keys;

/**
 * ThreadLocalTimeOffsetSupplier
 *
 * @version 1.0
 * Date: 2020-01-15
 */
public class ThreadLocalTimeOffsetSupplier implements Supplier<Long> {

    public static void put(Long timezoneOffsetMillis) {
        MapThreadLocalAdaptor.put(Keys.TIMEZONE_OFFSET_MILLIS, timezoneOffsetMillis);
    }

    public static void clear() {
        MapThreadLocalAdaptor.remove(Keys.TIMEZONE_OFFSET_MILLIS);
    }

    @Override
    public Long get() {
        final Object value = MapThreadLocalAdaptor.get(Keys.TIMEZONE_OFFSET_MILLIS);
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }

}
