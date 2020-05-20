package com.alpha.coding.common.bean.comm;

import java.util.Optional;

/**
 * SelfRefBean
 *
 * @version 1.0
 * Date: 2020-02-27
 */
public interface SelfRefBean<SELF extends SelfRefBean<SELF>> {

    default SELF self() {
        return Optional.ofNullable((SELF) SelfRefBeanDelegator.lookup(this)).orElse((SELF) this);
    }

}
