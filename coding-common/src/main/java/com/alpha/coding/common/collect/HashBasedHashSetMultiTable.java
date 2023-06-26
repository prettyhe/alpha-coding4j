package com.alpha.coding.common.collect;

import java.util.LinkedHashMap;

import com.google.common.collect.HashMultimap;

/**
 * HashBasedHashSetMultiTable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class HashBasedHashSetMultiTable<R, C, V> extends HashBasedMultiTable<R, C, V> {

    private static final long serialVersionUID = 7703841049683900487L;

    public HashBasedHashSetMultiTable() {
        super(new LinkedHashMap<>(), HashMultimap::create);
    }

    public static <R, C, V> HashBasedHashSetMultiTable<R, C, V> create() {
        return new HashBasedHashSetMultiTable<>();
    }
}
