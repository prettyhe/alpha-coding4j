package com.alpha.coding.common.aop.invoke;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;

/**
 * OnceDataHolderAspect
 *
 * @version 1.0
 * Date: 2021/9/22
 */
public class OnceDataHolderAspect {

    private static final String ASPECT_KEY = "$_doAspectCnt";

    @Getter
    @Setter
    private Boolean enableLog;

    public void doBefore() {
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(ASPECT_KEY);
        if (supplier == null) {
            OnceDataHolder.clear();
            if (enableLog == null) {
                OnceDataHolder.getOnce(ASPECT_KEY, () -> new AtomicInteger(1));
            } else {
                OnceDataHolder.getOnce(ASPECT_KEY, () -> new AtomicInteger(1), enableLog);
            }
        } else {
            supplier.get().incrementAndGet();
        }
    }

    public void doAfter() {
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(ASPECT_KEY);
        if (supplier == null || supplier.get().get() <= 1) {
            OnceDataHolder.clear();
        } else {
            supplier.get().decrementAndGet();
        }
    }

}
