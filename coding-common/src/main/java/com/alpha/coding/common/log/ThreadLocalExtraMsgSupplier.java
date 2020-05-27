package com.alpha.coding.common.log;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

/**
 * ThreadLocalExtraMsgSupplier
 *
 * @version 1.0
 * Date: 2020/5/14
 */
public class ThreadLocalExtraMsgSupplier implements ExtraMsgSupplier {

    public static final String KEY = "MonitorLogExtraMsg";

    private Supplier<String> supplier =
            () -> Optional.ofNullable(MapThreadLocalAdaptor.get(KEY)).map(String::valueOf).orElse((String) null);

    private Consumer<String> appender = s -> Optional.ofNullable(s)
            .ifPresent(p -> MapThreadLocalAdaptor.put(KEY, Optional.ofNullable(MapThreadLocalAdaptor.get(KEY))
                    .filter(Objects::nonNull).map(x -> x + p).orElse(p)));

    @Override
    public Supplier<String> supplier() {
        return supplier;
    }

    @Override
    public Consumer<String> appender() {
        return appender;
    }
}
