package com.alpha.coding.common.event.parser;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ParseSrcWrapper
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class ParseSrcWrapper {

    private Object[] args;
    private Object returnValue;

}
