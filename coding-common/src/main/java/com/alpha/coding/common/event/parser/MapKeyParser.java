package com.alpha.coding.common.event.parser;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * MapKeyParser
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class MapKeyParser implements EventKeyParser {

    @Override
    public Set parse(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map) obj).keySet();
        }
        return null;
    }

}
