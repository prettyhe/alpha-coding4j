package com.alpha.coding.common.bean.identity.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * IdentityBean
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IdentityBean {

    /**
     * bean的类型
     */
    Class<?> beanClass();

    /**
     * 身份识别的方法名，要求改方法：公共的，无参的，有返回值(即身份牌)
     */
    String identityMethod();

}
