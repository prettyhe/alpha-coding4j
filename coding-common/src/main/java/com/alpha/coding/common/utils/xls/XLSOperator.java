package com.alpha.coding.common.utils.xls;

import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.beanutils.ConvertUtils;

import com.alpha.coding.common.utils.convert.DateConverter;
import com.alpha.coding.common.utils.convert.NumberConverter;

/**
 * XLSOperator
 *
 * @version 1.0
 * Date: 2022/3/22
 */
public abstract class XLSOperator {

    static {
        Stream.of(Long.class, Integer.class, BigDecimal.class)
                .forEach(type -> {
                    ConvertUtils.deregister(type);
                    ConvertUtils.register(NumberConverter.getDefault(), type);
                });
        ConvertUtils.deregister(Date.class);
        ConvertUtils.register(new DateConverter(), Date.class);
    }

}
