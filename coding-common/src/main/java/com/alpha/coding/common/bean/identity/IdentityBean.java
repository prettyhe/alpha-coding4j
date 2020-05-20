package com.alpha.coding.common.bean.identity;

import java.util.function.Supplier;

/**
 * IdentityBean
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface IdentityBean {

    Supplier identity();

    Class<? extends IdentityBean> beanClz();

}
