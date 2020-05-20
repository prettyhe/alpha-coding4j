package com.alpha.coding.common.collect;

import org.apache.commons.collections4.map.HashedMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * HashBasedArrayListMultiTable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class HashBasedArrayListMultiTable<R, C, V> extends HashBasedMultiTable<R, C, V> {

    public HashBasedArrayListMultiTable() {
        super(new HashedMap<R, Multimap<C, V>>(), () -> (Multimap<C, V>) ArrayListMultimap.create());
    }

    public static <R, C, V> HashBasedArrayListMultiTable<R, C, V> create() {
        return new HashBasedArrayListMultiTable();
    }
}
