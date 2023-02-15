package com.alpha.coding.common.executor;

import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.MDC;

import com.alpha.coding.bo.base.MapThreadLocalAdaptor;
import com.alpha.coding.bo.executor.RunnableWrapper;
import com.alpha.coding.bo.function.impl.MDCClearConsumer;
import com.alpha.coding.bo.function.impl.MDCCopyConsumer;
import com.alpha.coding.bo.function.impl.MapThreadLocalAdaptorClearConsumer;
import com.alpha.coding.bo.function.impl.MapThreadLocalAdaptorCopyConsumer;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * MDCRunnableWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Accessors(chain = true)
public class MDCRunnableWrapper extends RunnableWrapper {

    private Map<String, String> superMDCContext;
    private Map<String, Object> superMapThreadLocalAdaptorContext;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> existMDCContext;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Object> existMapThreadLocalAdaptorContext;

    public static MDCRunnableWrapper of(Runnable runnable, Map<String, String> superMDCContext) {
        return new MDCRunnableWrapper(runnable, superMDCContext);
    }

    public static MDCRunnableWrapper of(Runnable runnable, Map<String, String> superMDCContext,
                                        Map<String, Object> superMapThreadLocalAdaptorContext) {
        return new MDCRunnableWrapper(runnable, superMDCContext, superMapThreadLocalAdaptorContext);
    }

    public static MDCRunnableWrapper of(Runnable runnable, Map<String, String> superMDCContext,
                                        Map<String, Object> superMapThreadLocalAdaptorContext,
                                        Consumer<Exception> whenException) {
        return new MDCRunnableWrapper(runnable, superMDCContext, superMapThreadLocalAdaptorContext, whenException);
    }

    public MDCRunnableWrapper(Runnable runnable, Map<String, String> superMDCContext) {
        this(runnable, superMDCContext, null, null);
    }

    public MDCRunnableWrapper(Runnable runnable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext) {
        this(runnable, superMDCContext, superMapThreadLocalAdaptorContext, null);
    }

    public MDCRunnableWrapper(Runnable runnable, Map<String, String> superMDCContext,
                              Map<String, Object> superMapThreadLocalAdaptorContext,
                              Consumer<Exception> whenException) {
        super(runnable);
        this.superMDCContext = superMDCContext;
        this.superMapThreadLocalAdaptorContext = superMapThreadLocalAdaptorContext;
        this.setBefore(() -> {
            // 先记录
            if (MDC.getMDCAdapter() != null) {
                MDCRunnableWrapper.this.existMDCContext = MDC.getCopyOfContextMap();
            }
            MDCRunnableWrapper.this.existMapThreadLocalAdaptorContext = MapThreadLocalAdaptor.getCopyOfContextMap();
            // 再设置
            MDCCopyConsumer.getInstance().accept(MDCRunnableWrapper.this.superMDCContext);
            MapThreadLocalAdaptorCopyConsumer.getInstance()
                    .accept(MDCRunnableWrapper.this.superMapThreadLocalAdaptorContext);
        });
        this.setAfter(() -> {
            // 先清除
            MDCClearConsumer.getInstance().accept(MDCRunnableWrapper.this.superMDCContext);
            MapThreadLocalAdaptorClearConsumer.getInstance()
                    .accept(MDCRunnableWrapper.this.superMapThreadLocalAdaptorContext);
            // 再还原
            MDCCopyConsumer.getInstance().accept(MDCRunnableWrapper.this.existMDCContext);
            MapThreadLocalAdaptorCopyConsumer.getInstance()
                    .accept(MDCRunnableWrapper.this.existMapThreadLocalAdaptorContext);
        });
        this.setWhenException(whenException);
    }

}
