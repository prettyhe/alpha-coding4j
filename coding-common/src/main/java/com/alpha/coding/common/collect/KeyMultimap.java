package com.alpha.coding.common.collect;

import java.io.Serializable;

import com.google.common.collect.Multimap;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * KeyMultimap
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class KeyMultimap<R, C, V> implements Serializable {

    private R key;
    private Multimap<C, V> multimap;

}
