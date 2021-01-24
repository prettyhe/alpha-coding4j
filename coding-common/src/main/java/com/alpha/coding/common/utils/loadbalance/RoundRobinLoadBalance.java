package com.alpha.coding.common.utils.loadbalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundRobinLoadBalance 简单的加权轮询负载均衡
 *
 * @version 1.0
 * Date: 2020-03-23
 */
public class RoundRobinLoadBalance<S> {

    private final ConcurrentMap<S, AtomicInteger> INVOKE_COUNT_MAP = new ConcurrentHashMap<>();
    private final ConcurrentMap<S, AtomicInteger> CURRENT_WEIGHT_MAP = new ConcurrentHashMap<>();
    private final ConcurrentMap<S, AtomicInteger> REMAIN_WEIGHT_MAP = new ConcurrentHashMap<>();

    private final List<S> services;
    private final List<Integer> weights;

    public RoundRobinLoadBalance(List<S> services, List<Integer> weights) {
        Objects.requireNonNull(services);
        Objects.requireNonNull(weights);
        if (services.size() != weights.size()) {
            throw new IllegalArgumentException("size of services must equal to size of weights");
        }
        this.services = services;
        this.weights = weights;
    }

    /**
     * 选择服务
     */
    public S select() {
        final Integer totalWeight = weights.stream().reduce(0, Integer::sum);
        int maxWeight = Integer.MIN_VALUE;
        int maxWeightIndex = 0;
        for (int i = 0; i < services.size(); i++) {
            int initWeight = weights.get(i);
            REMAIN_WEIGHT_MAP.computeIfAbsent(services.get(i), k -> new AtomicInteger(0));
            final AtomicInteger currWeight =
                    CURRENT_WEIGHT_MAP.computeIfAbsent(services.get(i), k -> new AtomicInteger(initWeight));
            if (currWeight.get() > maxWeight) {
                maxWeight = currWeight.get();
                maxWeightIndex = i;
            }
        }
        CURRENT_WEIGHT_MAP.get(services.get(maxWeightIndex)).addAndGet(-totalWeight);
        CURRENT_WEIGHT_MAP.forEach((k, v) -> REMAIN_WEIGHT_MAP.get(k).set(v.get()));
        for (int i = 0; i < services.size(); i++) {
            CURRENT_WEIGHT_MAP.get(services.get(i)).set(weights.get(i) + REMAIN_WEIGHT_MAP.get(services.get(i)).get());
        }
        INVOKE_COUNT_MAP.computeIfAbsent(services.get(maxWeightIndex), x -> new AtomicInteger(0)).incrementAndGet();
        return services.get(maxWeightIndex);
    }

    /**
     * 查看调用次数
     */
    public Map<S, Integer> getSelectCount() {
        final HashMap<S, Integer> hashMap = new HashMap<>(INVOKE_COUNT_MAP.size());
        INVOKE_COUNT_MAP.forEach((k, v) -> hashMap.put(k, v.get()));
        return hashMap;
    }

}
