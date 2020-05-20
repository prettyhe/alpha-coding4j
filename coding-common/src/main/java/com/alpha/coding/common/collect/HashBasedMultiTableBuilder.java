package com.alpha.coding.common.collect;

/**
 * HashBasedMultiTableBuilder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class HashBasedMultiTableBuilder {

    public static <R, C, V> HashBasedArrayListMultiTable<R, C, V> createArrayListMultiTable() {
        return new HashBasedArrayListMultiTable<>();
    }

    public static <R, C, V> HashBasedHashSetMultiTable<R, C, V> createHashMultiTable() {
        return new HashBasedHashSetMultiTable<>();
    }

}
