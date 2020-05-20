package com.alpha.coding.common.executor;

import java.util.Map;

import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.function.impl.MDCClearConsumer;
import com.alpha.coding.bo.function.impl.MDCCopyConsumer;
import com.alpha.coding.bo.function.impl.MapThreadLocalAdaptorClearConsumer;
import com.alpha.coding.bo.function.impl.MapThreadLocalAdaptorCopyConsumer;

import lombok.experimental.Accessors;

/**
 * MDCRunnableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Accessors(chain = true)
public class MDCRunnableWrapper extends RunnableWrapper implements Runnable {

    private Map<String, String> superMDCContext;
    private Map<String, Object> superMapThreadLocalAdaptorContext;

    public MDCRunnableWrapper(Runnable runnable, Map<String, String> superMDCContext) {
        this(runnable, superMDCContext, null);
    }

    public MDCRunnableWrapper(Runnable runnable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext) {
        super(runnable);
        this.superMDCContext = superMDCContext;
        this.superMapThreadLocalAdaptorContext = superMapThreadLocalAdaptorContext;
        this.setBefore(() -> {
            new MDCCopyConsumer().accept(MDCRunnableWrapper.this.superMDCContext);
            new MapThreadLocalAdaptorCopyConsumer()
                    .accept(MDCRunnableWrapper.this.superMapThreadLocalAdaptorContext);
        });
        this.setAfter(() -> {
            new MDCClearConsumer().accept(MDCRunnableWrapper.this.superMDCContext);
            new MapThreadLocalAdaptorClearConsumer()
                    .accept(MDCRunnableWrapper.this.superMapThreadLocalAdaptorContext);
        });
    }

}
