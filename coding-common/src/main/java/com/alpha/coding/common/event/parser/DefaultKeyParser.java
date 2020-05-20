/**
 * Copyright
 */
package com.alpha.coding.common.event.parser;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * DefaultKeyParser 默认的事件key解析器
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class DefaultKeyParser implements EventKeyParser {

    @Override
    public Set parse(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Iterable) {
            return Sets.newHashSet((Iterable) obj);
        }
        if (obj instanceof Map) {
            return ((Map) obj).keySet();
        }
        if (obj instanceof Multimap) {
            return ((Multimap) obj).keySet();
        }
        return Sets.newHashSet(obj);
    }

}
