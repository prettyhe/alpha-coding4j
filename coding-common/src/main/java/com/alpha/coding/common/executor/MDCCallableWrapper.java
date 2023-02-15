package com.alpha.coding.common.executor;

import java.util.Map;
import java.util.concurrent.Callable;

import com.alpha.coding.bo.executor.CallableToRunnableWrapper;
import com.alpha.coding.bo.executor.CallableWrapper;
import com.alpha.coding.bo.function.ThrowableFunction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * MDCCallableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MDCCallableWrapper<V> extends CallableWrapper<V> {

    private static <V> Callable<V> wrap(Callable<V> callable, Map<String, String> superMDCContext,
                                        Map<String, Object> superMapThreadLocalAdaptorContext,
                                        ThrowableFunction<Exception, V> whenException) {
        final CallableToRunnableWrapper<V> runnableWrapper = CallableToRunnableWrapper.wrap(callable, whenException);
        final MDCRunnableWrapper runnable = MDCRunnableWrapper.of(runnableWrapper, superMDCContext,
                superMapThreadLocalAdaptorContext);
        return () -> {
            runnable.run();
            if (runnableWrapper.hasException()) {
                throw runnableWrapper.getException();
            }
            return runnableWrapper.getResult();
        };
    }

    public static <V> MDCCallableWrapper<V> of(Callable<V> callable, Map<String, String> superMDCContext) {
        return new MDCCallableWrapper<>(callable, superMDCContext);
    }

    public static <V> MDCCallableWrapper<V> of(Callable<V> callable, Map<String, String> superMDCContext,
                                               Map<String, Object> superMapThreadLocalAdaptorContext) {
        return new MDCCallableWrapper<>(callable, superMDCContext, superMapThreadLocalAdaptorContext);
    }

    public static <V> MDCCallableWrapper<V> of(Callable<V> callable, Map<String, String> superMDCContext,
                                               Map<String, Object> superMapThreadLocalAdaptorContext,
                                               ThrowableFunction<Exception, V> whenException) {
        return new MDCCallableWrapper<>(callable, superMDCContext, superMapThreadLocalAdaptorContext, whenException);
    }

    public MDCCallableWrapper(Callable<V> callable, Map<String, String> superMDCContext) {
        super(wrap(callable, superMDCContext, null, null));
    }

    public MDCCallableWrapper(Callable<V> callable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext) {
        super(wrap(callable, superMDCContext, superMapThreadLocalAdaptorContext, null));
    }

    public MDCCallableWrapper(Callable<V> callable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext,
                              ThrowableFunction<Exception, V> whenException) {
        super(wrap(callable, superMDCContext, superMapThreadLocalAdaptorContext, whenException));
    }

}
