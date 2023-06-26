package com.alpha.coding.common.collect;

import java.util.LinkedHashMap;

import com.google.common.collect.ArrayListMultimap;

/**
 * HashBasedArrayListMultiTable
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class HashBasedArrayListMultiTable<R, C, V> extends HashBasedMultiTable<R, C, V> {

    private static final long serialVersionUID = -4527209983018123630L;

    public HashBasedArrayListMultiTable() {
        super(new LinkedHashMap<>(), ArrayListMultimap::create);
    }

    public static <R, C, V> HashBasedArrayListMultiTable<R, C, V> create() {
        return new HashBasedArrayListMultiTable<>();
    }
}
