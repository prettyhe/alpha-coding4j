package com.alpha.coding.bo.assist.ref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * DefaultRefContext
 *
 * @version 1.0
 * Date: 2020/4/14
 */
public class DefaultRefContext implements RefContext {

    private ConcurrentMap<Class, List> CACHE = new ConcurrentHashMap<>();

    @Override
    public void register(Class type, Object bean) {
        if (bean == null) {
            return;
        }
        Consumer<Class> register = s -> {
            final List list = CACHE.computeIfAbsent(s, t -> new ArrayList<>());
            synchronized(CACHE) {
                boolean hasRegistered = false;
                for (final Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                    final Object next = iterator.next();
                    if (next == bean) {
                        hasRegistered = true;
                        break;
                    }
                }
                if (!hasRegistered) {
                    list.add(bean);
                }
            }
        };
        Set<Class> set = new LinkedHashSet<>();
        findTypes(type, set);
        set.forEach(register);
    }

    @Override
    public void unregister(Object bean) {
        if (bean == null) {
            return;
        }
        Set<Class> set = new LinkedHashSet<>();
        findTypes(bean.getClass(), set);
        set.forEach(type -> {
            final List refs = CACHE.get(type);
            if (refs == null) {
                return;
            }
            synchronized(CACHE) {
                for (final Iterator iterator = refs.iterator(); iterator.hasNext(); ) {
                    if (iterator.next() == bean) {
                        iterator.remove();
                    }
                }
                if (refs.isEmpty()) {
                    CACHE.remove(type);
                }
            }
        });
    }

    @Override
    public void unregister(Class type) {
        CACHE.remove(type);
    }

    @Override
    public Map<Class, List> getAll() {
        Map<Class, List> map = new HashMap<>();
        for (Class clz : CACHE.keySet()) {
            map.put(clz, map.get(clz));
        }
        return map;
    }

    @Override
    public void clear() {
        CACHE.clear();
    }

    @Override
    public List lookup(Class type) {
        return CACHE.get(type);
    }

    protected void findTypes(Class beanClass, Set<Class> set) {
        set.add(beanClass);
        final Class superclass = beanClass.getSuperclass();
        if (superclass != null && !superclass.getName().startsWith("java.")
                && !superclass.getName().startsWith("javax.")) {
            findTypes(superclass, set);
        }
        Optional.ofNullable(beanClass.getInterfaces())
                .ifPresent(p -> Arrays.stream(p).forEach(i -> findTypes(i, set)));
    }
}
