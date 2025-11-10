package com.alpha.coding.common.log;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;

import lombok.Setter;

/**
 * ThreadLocalExtraMsgSupplier
 *
 * @version 1.0
 * Date: 2020/5/14
 */
@Setter
public class ThreadLocalExtraMsgSupplier implements ExtraMsgSupplier {

    public static final String KEY = "MonitorLogExtraMsg";

    private Supplier<String> supplier = () -> MapThreadLocalAdaptor.getAsString(KEY);

    private Consumer<String> appender = s -> Optional.ofNullable(s)
            .ifPresent(p -> MapThreadLocalAdaptor.put(KEY, Optional.ofNullable(MapThreadLocalAdaptor.get(KEY))
                    .map(x -> x + p).orElse(p)));

    private Consumer<String> aheadAppender = s -> Optional.ofNullable(s)
            .ifPresent(p -> MapThreadLocalAdaptor.put(KEY, Optional.ofNullable(MapThreadLocalAdaptor.get(KEY))
                    .map(x -> p + x).orElse(p)));

    @Override
    public Supplier<String> supplier() {
        return supplier;
    }

    @Override
    public Consumer<String> appender() {
        return appender;
    }

    @Override
    public Consumer<String> aheadAppender() {
        return aheadAppender;
    }
}
