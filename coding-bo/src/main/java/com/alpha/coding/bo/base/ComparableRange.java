package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ComparableRange
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class ComparableRange implements Serializable {

    @SuppressWarnings({"rawtypes"})
    private Comparable min;
    @SuppressWarnings({"rawtypes"})
    private Comparable max;

}
