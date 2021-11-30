package com.alpha.coding.common.event.parser;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;

/**
 * MultimapKeyParser
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Component
public class MultimapKeyParser implements EventKeyParser {

    @Override
    public Set parse(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Multimap) {
            return ((Multimap) obj).keySet();
        }
        return null;
    }

}
