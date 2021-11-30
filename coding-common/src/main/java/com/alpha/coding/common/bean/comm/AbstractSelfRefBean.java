package com.alpha.coding.common.bean.comm;

import lombok.Setter;

/**
 * AbstractSelfRefBean
 *
 * <p>
 * 包含一个对自身的引用的bean
 * </p>
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class AbstractSelfRefBean<SELF extends AbstractSelfRefBean<SELF>> {

    @Setter
    private SELF self;

    protected SELF self() {
        return self;
    }

}
