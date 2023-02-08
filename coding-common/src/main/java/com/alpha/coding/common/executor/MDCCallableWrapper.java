package com.alpha.coding.common.executor;

import java.util.Map;
import java.util.concurrent.Callable;

import lombok.Data;
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
public class MDCCallableWrapper<V> implements Callable<V> {

    private Callable<V> callable;
    private Map<String, String> superMDCContext;
    private Map<String, Object> superMapThreadLocalAdaptorContext;

    public MDCCallableWrapper(Callable<V> callable, Map<String, String> superMDCContext) {
        this.callable = callable;
        this.superMDCContext = superMDCContext;
    }

    public MDCCallableWrapper(Callable<V> callable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext) {
        this.callable = callable;
        this.superMDCContext = superMDCContext;
        this.superMapThreadLocalAdaptorContext = superMapThreadLocalAdaptorContext;
    }

    @Override
    public V call() throws Exception {
        final Object[] ret = new Object[1];
        final Exception[] exceptions = new Exception[1];
        new MDCRunnableWrapper(() -> {
            try {
                ret[0] = callable.call();
            } catch (Exception e) {
                exceptions[0] = e;
            }
        }, superMDCContext, superMapThreadLocalAdaptorContext).run();
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
        return (V) ret[0];
    }

}
