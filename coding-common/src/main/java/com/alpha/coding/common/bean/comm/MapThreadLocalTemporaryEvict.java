package com.alpha.coding.common.bean.comm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MapThreadLocalTemporaryEvict
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface MapThreadLocalTemporaryEvict {

    String[] value();
}
