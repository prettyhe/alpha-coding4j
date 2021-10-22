package com.alpha.coding.common.aop.invoke;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * OnceDataHolderAspect
 *
 * @version 1.0
 * Date: 2021/9/22
 */
public class OnceDataHolderAspect {

    private static final String ASPECT_KEY = "$_doAspectCnt";

    public void doBefore() {
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(ASPECT_KEY);
        if (supplier == null) {
            OnceDataHolder.clear();
            OnceDataHolder.getOnce(ASPECT_KEY, () -> new AtomicInteger(1));
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
