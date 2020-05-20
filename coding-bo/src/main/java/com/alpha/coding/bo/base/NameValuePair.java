/**
 * Copyright
 */
package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * NameValuePair
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NameValuePair<N, V> implements Serializable {

    private N name;
    private V value;

}
