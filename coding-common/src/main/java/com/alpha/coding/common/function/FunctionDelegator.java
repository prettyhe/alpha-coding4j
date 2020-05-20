package com.alpha.coding.common.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.MDC;

import com.alpha.coding.bo.constant.Keys;
import com.alpha.coding.bo.function.NullSupplier;
import com.alpha.coding.bo.trace.TraceIdGenerator;
import com.google.common.base.Stopwatch;

import lombok.extern.slf4j.Slf4j;

/**
 * FunctionDelegator
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class FunctionDelegator {

    /**
     * 执run，不关心异常
     */
    public static void tryRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            // nothing
        }
    }

    /**
     * 执行accept，不关心异常
     */
    public static void tryAccept(Consumer consumer, Object object) {
        try {
            consumer.accept(object);
        } catch (Exception e) {
            // nothing
        }
    }

    /**
     * 执行apply，不关心异常
     */
    public static <T, R> R tryApply(Function<T, R> function, T t) {
        return tryApply(function, t, null);
    }

    /**
     * 执行apply，不关心异常
     */
    public static <T, R> R tryApply(Function<T, R> function, T t, R defaultReturn) {
        try {
            return function.apply(t);
        } catch (Exception e) {
            // nothing
        }
        return defaultReturn;
    }

    public static void timeRun(Runnable runnable, Consumer<Stopwatch> timeConsume) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            runnable.run();
        } finally {
            stopwatch.stop();
            if (timeConsume != null) {
                timeConsume.accept(stopwatch);
            } else {
                log.info("Runnable cost {}", stopwatch.toString());
            }
        }
    }

    public static void timeAccept(Consumer consumer, Object object, Consumer<Stopwatch> timeConsume) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            consumer.accept(object);
        } finally {
            stopwatch.stop();
            if (timeConsume != null) {
                timeConsume.accept(stopwatch);
            } else {
                log.info("Consumer cost {}", stopwatch.toString());
            }
        }
    }

    public static <T, R> R timeApply(Function<T, R> function, T t, Consumer<Stopwatch> timeConsume) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            return function.apply(t);
        } finally {
            stopwatch.stop();
            if (timeConsume != null) {
                timeConsume.accept(stopwatch);
            } else {
                log.info("Function cost {}", stopwatch.toString());
            }
        }
    }

    /**
     * 对一段function使用新的trace
     */
    public static <T> T traceAgent(Supplier<T> supplier, TraceIdGenerator generator) {
        if (MDC.getMDCAdapter() == null || generator == null) {
            return supplier.get();
        } else {
            try {
                MDC.put(Keys.TRACE_ID, generator.traceId());
                return supplier.get();
            } finally {
                MDC.remove(Keys.TRACE_ID);
            }
        }
    }

    /**
     * 对一段function使用新的trace，若已有trace则忽略
     */
    public static <T> T traceAgentIfAbsent(Supplier<T> supplier, TraceIdGenerator generator) {
        if (MDC.getMDCAdapter() == null || generator == null || MDC.get(Keys.TRACE_ID) != null) {
            return supplier.get();
        } else {
            try {
                MDC.put(Keys.TRACE_ID, generator.traceId());
                return supplier.get();
            } finally {
                MDC.remove(Keys.TRACE_ID);
            }
        }
    }

    public static void traceRun(final Runnable runnable, TraceIdGenerator generator) {
        traceAgent(new Supplier<Object>() {
            @Override
            public Object get() {
                runnable.run();
                return null;
            }
        }, generator);
    }

    public static void traceRunIfAbsent(final Runnable runnable, TraceIdGenerator generator) {
        traceAgentIfAbsent(new NullSupplier<>(runnable), generator);
    }

    public static void traceAccept(final Consumer consumer, final Object object, TraceIdGenerator generator) {
        traceAgent(new NullSupplier<>(() -> consumer.accept(object)), generator);
    }

    public static void traceAcceptIfAbsent(final Consumer consumer, final Object object, TraceIdGenerator generator) {
        traceAgentIfAbsent(new NullSupplier<>(() -> consumer.accept(object)), generator);
    }

    public static <T, R> R traceApply(final Function<T, R> function, final T t, TraceIdGenerator generator) {
        return traceAgent(() -> function.apply(t), generator);
    }

    public static <T, R> R traceApplyIfAbsent(final Function<T, R> function, final T t, TraceIdGenerator generator) {
        return traceAgentIfAbsent(() -> function.apply(t), generator);
    }

}
