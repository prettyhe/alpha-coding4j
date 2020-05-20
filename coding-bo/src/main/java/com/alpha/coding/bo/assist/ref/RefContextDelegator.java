package com.alpha.coding.bo.assist.ref;

/**
 * SelfRefContextDelegator
 *
 * @version 1.0
 * Date: 2020/4/14
 */
public final class RefContextDelegator {

    private static final RefContext delegator = new DefaultRefContext();

    static RefContext getDelegator() {
        return delegator;
    }

}
