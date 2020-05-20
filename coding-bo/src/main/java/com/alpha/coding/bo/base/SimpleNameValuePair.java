/**
 * Copyright
 */
package com.alpha.coding.bo.base;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SimpleNameValuePair
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SimpleNameValuePair extends NameValuePair<String, String> implements Serializable {

    public SimpleNameValuePair() {
        super();
    }

    public SimpleNameValuePair(String name, String value) {
        super(name, value);
    }

}
