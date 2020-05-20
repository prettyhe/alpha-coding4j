package com.alpha.coding.common.collect;

import org.apache.commons.collections4.map.HashedMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * HashBasedHashSetMultiTable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class HashBasedHashSetMultiTable<R, C, V> extends HashBasedMultiTable<R, C, V> {

    public HashBasedHashSetMultiTable() {
        super(new HashedMap<R, Multimap<C, V>>(), () -> (Multimap<C, V>) HashMultimap.create());
    }

    public static <R, C, V> HashBasedHashSetMultiTable<R, C, V> create() {
        return new HashBasedHashSetMultiTable();
    }
}
