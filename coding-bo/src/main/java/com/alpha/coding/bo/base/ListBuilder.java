package com.alpha.coding.bo.base;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * ListBuilder
 *
 * @version 1.0
 * Date: 2020/5/9
 */
public class ListBuilder<E> {

    private final List<E> list;

    private ListBuilder(List<E> list) {
        this.list = list;
    }

    public static <E> ListBuilder<E> of(List<E> list) {
        return new ListBuilder<>(list);
    }

    public static <E> ListBuilder<E> of(Supplier<List<E>> supplier) {
        return new ListBuilder<>(supplier.get());
    }

    public ListBuilder<E> add(E e) {
        this.list.add(e);
        return this;
    }

    public ListBuilder<E> addAll(Collection<? extends E> c) {
        this.list.addAll(c);
        return this;
    }

    public ListBuilder<E> remove(E e) {
        this.list.remove(e);
        return this;
    }

    public List<E> build() {
        return this.list;
    }

}
