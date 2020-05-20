package com.alpha.coding.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * HashBasedMultiTable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@GwtCompatible
@RequiredArgsConstructor
public class HashBasedMultiTable<R, C, V> implements Serializable {

    @NonNull
    final Map<R, Multimap<C, V>> backingMap;
    @NonNull
    final Supplier<? extends Multimap<C, V>> factory;

    public boolean contains(@Nullable Object rowKey) {
        return rowKey != null && backingMap.containsKey(rowKey);
    }

    public boolean contains(@Nullable Object rowKey, @Nullable Object columnKey) {
        return rowKey != null && columnKey != null && backingMap.containsKey(rowKey)
                && backingMap.get(rowKey).containsKey(columnKey);
    }

    public Collection<V> get(@Nullable R rowKey, @Nullable C columnKey) {
        if (contains(rowKey, columnKey)) {
            return backingMap.get(rowKey).get(columnKey);
        }
        return null;
    }

    public Multimap<C, V> get(@Nullable R rowKey) {
        if (contains(rowKey)) {
            return backingMap.get(rowKey);
        }
        return null;
    }

    public Set<R> rowKeys() {
        return backingMap.keySet();
    }

    public Collection<Collection<V>> values() {
        List<Collection<V>> ret = Lists.newArrayList();
        for (Multimap<C, V> map : backingMap.values()) {
            ret.add(map.values());
        }
        return ret;
    }

    public Multimap<R, C> mappings() {
        Multimap<R, C> ret = ArrayListMultimap.create();
        for (R rowKey : rowKeys()) {
            ret.putAll(rowKey, backingMap.get(rowKey).keySet());
        }
        return ret;
    }

    public boolean put(R rowKey, C columnKey, V value) {
        checkNotNull(rowKey);
        checkNotNull(columnKey);
        checkNotNull(value);
        return getOrCreate(rowKey).put(columnKey, value);
    }

    public Collection<V> remove(@Nullable R rowKey, @Nullable C columnKey) {
        if (rowKey == null || columnKey == null) {
            return null;
        }
        Multimap<C, V> map = backingMap.get(rowKey);
        if (map == null) {
            return null;
        }
        Collection<V> ret = map.removeAll(columnKey);
        if (map.isEmpty()) {
            backingMap.remove(rowKey);
        }
        return ret;
    }

    public boolean removeColumn(@Nullable R rowKey, @Nullable C columnKey, @Nullable V value) {
        if (rowKey == null || columnKey == null || value == null) {
            return false;
        }
        Multimap<C, V> map = backingMap.get(rowKey);
        if (map == null) {
            return false;
        }
        boolean ret = map.remove(columnKey, value);
        if (map.isEmpty()) {
            backingMap.remove(rowKey);
        }
        return ret;
    }

    public void clear() {
        backingMap.clear();
    }

    public int size() {
        int size = 0;
        for (Multimap<C, V> map : backingMap.values()) {
            size += map.size();
        }
        return size;
    }

    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    private Multimap<C, V> getOrCreate(R rowKey) {
        Multimap<C, V> map = backingMap.get(rowKey);
        if (map == null) {
            map = factory.get();
            backingMap.put(rowKey, map);
        }
        return map;
    }

    public <TR extends R, TC extends C, TV extends V> void putAll(HashBasedMultiTable<TR, TC, TV> table) {
        final Iterator<TR> iterator = table.rowKeys().iterator();
        while (iterator.hasNext()) {
            final TR next = iterator.next();
            final Multimap<? extends C, ? extends V> multimap = table.get(next);
            if (multimap != null) {
                this.getOrCreate(next).putAll(multimap);
            }
        }
    }

    public <TR extends R, TC extends C, TV extends V> void putAll(TR rowKey, Multimap<TC, TV> multimap) {
        checkNotNull(rowKey);
        if (multimap != null) {
            this.getOrCreate(rowKey).putAll(multimap);
        }
    }

}
