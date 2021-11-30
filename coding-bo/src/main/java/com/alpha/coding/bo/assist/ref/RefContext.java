package com.alpha.coding.bo.assist.ref;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * RefContext
 *
 * @version 1.0
 * Date: 2020/4/14
 */
public interface RefContext {

    /**
     * 注册
     */
    void register(Class type, Object bean);

    /**
     * 注册
     */
    default void register(Object bean) {
        register(bean.getClass(), bean);
    }

    /**
     * 取消注册
     */
    void unregister(Object bean);

    /**
     * 取消注册
     */
    void unregister(Class type);

    /**
     * 获取所有
     */
    Map<Class, List> getAll();

    /**
     * 清空
     */
    void clear();

    /**
     * 查找
     */
    List lookup(Class type);

    /**
     * 查找任意
     */
    default Object findAny(Class type, Predicate predicate) {
        final List list = lookup(type);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().filter(predicate).findAny().orElse(null);
    }

    /**
     * 查找第一个
     */
    default Object findFirst(Class type, Predicate predicate) {
        final List list = lookup(type);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 合并
     */
    default void merge(RefContext context) {
        context.getAll().entrySet().stream()
                .map(Map.Entry::getValue)
                .forEach(p -> p.forEach(this::register));
    }

}
