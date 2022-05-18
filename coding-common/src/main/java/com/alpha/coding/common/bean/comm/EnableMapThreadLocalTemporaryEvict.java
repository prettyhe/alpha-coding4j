package com.alpha.coding.common.bean.comm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * EnableMapThreadLocalTemporaryEvict
 *
 * @version 1.0
 * Date: 2020-03-19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(MapThreadLocalTemporaryEvictConfiguration.class)
public @interface EnableMapThreadLocalTemporaryEvict {
}
