/**
 * Copyright
 */
package com.alpha.coding.bo.config;

import java.util.Iterator;
import java.util.List;

import lombok.Data;

/**
 * ListConfig
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class ListConfig<E> implements Iterable<E> {

    private Class<E> elementClass;

    private List<E> list;

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.size() == 0;
    }

    public E get(int index) {
        return list.get(index);
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

}
