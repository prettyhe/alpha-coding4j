package com.alpha.coding.bo.executor;

import java.util.Map;
import java.util.Optional;

import org.slf4j.MDC;

/**
 * MDCTaskDecorator
 *
 * @version 1.0
 * @date 2025年05月14日
 */
public class MDCTaskDecorator implements Runnable {

    private final Runnable runnable;
    private final Map<String, String> mdcCopyOfContextMap;

    private MDCTaskDecorator(Runnable runnable, Map<String, String> mdcCopyOfContextMap) {
        this.runnable = runnable;
        this.mdcCopyOfContextMap = mdcCopyOfContextMap;
    }

    public static MDCTaskDecorator of(Runnable runnable) {
        return of(runnable, MDC.getMDCAdapter() == null ? null : MDC.getCopyOfContextMap());
    }

    public static MDCTaskDecorator of(Runnable runnable, Map<String, String> mdcVopyOfContextMap) {
        if (runnable == null) {
            return null;
        }
        return new MDCTaskDecorator(runnable, mdcVopyOfContextMap);
    }

    @Override
    public void run() {
        Map<String, String> mdcCopyOfContextMapSnapshot = null;
        if (MDC.getMDCAdapter() != null) {
            // 保存快照
            mdcCopyOfContextMapSnapshot = MDC.getCopyOfContextMap();
            // 写入传递过来的值
            Optional.ofNullable(mdcCopyOfContextMap).ifPresent(m -> m.forEach(MDC::put));
        }
        try {
            this.runnable.run();
        } finally {
            if (MDC.getMDCAdapter() != null) {
                // 恢复快照
                MDC.clear();
                Optional.ofNullable(mdcCopyOfContextMapSnapshot).ifPresent(m -> m.forEach(MDC::put));
            }
        }
    }

}
