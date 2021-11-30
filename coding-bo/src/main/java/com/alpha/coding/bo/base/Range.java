package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Range
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Range implements Serializable {

    private String min;
    private String max;

}
