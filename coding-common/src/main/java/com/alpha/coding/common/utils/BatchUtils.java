package com.alpha.coding.common.utils;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * BatchUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class BatchUtils {

    public static interface BatchProcessCallback<V, T> {

        /**
         * 单批处理逻辑
         */
        T process(Collection<V> coll);

        /**
         * 多批处理结果合并
         */
        T merge(Collection<T> ts);

    }

    public static interface BatchCallback<V> {

        /**
         * 单批处理逻辑
         */
        void process(Collection<V> coll);

    }

    /**
     * 批量处理
     *
     * @param coll      待处理数据
     * @param batchSize 每批容量
     * @param callback  批回调
     */
    public static <T, V> T batchProcess(Collection<V> coll, int batchSize, BatchProcessCallback<V, T> callback) {
        if (coll == null || coll.isEmpty()) {
            return null;
        }
        Preconditions.checkArgument(batchSize > 0, "batchSize must greater than 0");
        Preconditions.checkNotNull(callback, "callback must not null");
        if (coll.size() <= batchSize) {
            return callback.process(coll);
        } else {
            List<V> oriList = coll instanceof List ? (List<V>) coll : Lists.newArrayList(coll);
            List<List<V>> collLists = Lists.partition(oriList, batchSize);
            List<T> rets = Lists.newArrayList();
            for (List<V> list : collLists) {
                T t = callback.process(list);
                rets.add(t);
            }
            return callback.merge(rets);
        }
    }

    /**
     * 批量处理
     *
     * @param coll      待处理数据
     * @param batchSize 每批容量
     * @param callback  批回调
     */
    public static <V> void batchProcess(Collection<V> coll, int batchSize, BatchCallback<V> callback) {
        if (coll == null || coll.isEmpty()) {
            return;
        }
        Preconditions.checkArgument(batchSize > 0, "batchSize must greater than 0");
        Preconditions.checkNotNull(callback, "callback must not null");
        if (coll.size() <= batchSize) {
            callback.process(coll);
        } else {
            List<V> oriList = coll instanceof List ? (List<V>) coll : Lists.newArrayList(coll);
            List<List<V>> collLists = Lists.partition(oriList, batchSize);
            for (List<V> list : collLists) {
                callback.process(list);
            }
        }
    }

}
