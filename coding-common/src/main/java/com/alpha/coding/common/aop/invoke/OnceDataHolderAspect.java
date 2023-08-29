package com.alpha.coding.common.aop.invoke;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * OnceDataHolderAspect
 *
 * @version 1.0
 * Date: 2021/9/22
 */
@Slf4j
public class OnceDataHolderAspect {

    private static final String ASPECT_KEY = "$_doAspectCnt";
    private static final OnceDataHolderAspect INSTANCE = new OnceDataHolderAspect();

    @Getter
    @Setter
    private Boolean enableLog;

    public static OnceDataHolderAspect getDefault() {
        return INSTANCE;
    }

    public void doBefore() {
        final String aspectCountKey = ASPECT_KEY + "_" + Thread.currentThread().getId();
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(aspectCountKey);
        if (supplier == null) {
            OnceDataHolder.clear();
            if (enableLog == null) {
                OnceDataHolder.getOnce(aspectCountKey, () -> new AtomicInteger(1));
            } else {
                OnceDataHolder.getOnce(aspectCountKey, () -> new AtomicInteger(1), enableLog);
            }
        } else {
            supplier.get().incrementAndGet();
        }
        if (log.isDebugEnabled()) {
            log.debug("OnceDataHolder after doBefore: aspectCnt => {}, keys => {}", fetchAspectCnt(),
                    Optional.ofNullable(OnceDataHolder.getKeys()).orElse(null));
        }
    }

    public void doAfter() {
        final String aspectCountKey = ASPECT_KEY + "_" + Thread.currentThread().getId();
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(aspectCountKey);
        if (supplier == null || supplier.get().get() <= 1) {
            OnceDataHolder.clear();
        } else {
            supplier.get().decrementAndGet();
        }
        if (log.isDebugEnabled()) {
            log.debug("OnceDataHolder after doAfter: aspectCnt => {}, keys => {}", fetchAspectCnt(),
                    Optional.ofNullable(OnceDataHolder.getKeys()).orElse(null));
        }
    }

    private Integer fetchAspectCnt() {
        final String aspectCountKey = ASPECT_KEY + "_" + Thread.currentThread().getId();
        final Supplier<AtomicInteger> supplier = OnceDataHolder.getSupplier(aspectCountKey);
        return Optional.ofNullable(supplier).map(Supplier::get).map(AtomicInteger::get).orElse(null);
    }

}
