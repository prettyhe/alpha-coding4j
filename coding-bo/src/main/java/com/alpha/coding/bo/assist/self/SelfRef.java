package com.alpha.coding.bo.assist.self;

import java.util.List;

import com.alpha.coding.bo.assist.ref.InjectRefType;
import com.alpha.coding.bo.assist.ref.anno.InjectRef;

/**
 * Self
 *
 * @version 1.0
 * Date: 2020/4/14
 */
@InjectRef
public interface SelfRef<SELF extends SelfRef<SELF>> extends InjectRef {

    default List<SELF> SELF_LIST() {
        return (List<SELF>) InjectRefType.GLOBAL.getContextVisitor().visit().lookup(this.getClass());
    }

}
