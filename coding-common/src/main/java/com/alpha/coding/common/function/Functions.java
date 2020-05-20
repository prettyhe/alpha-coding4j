package com.alpha.coding.common.function;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Functions
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface Functions {

    /**
     * 智能转json
     */
    Function<Object, String> smartToJson = t -> t == null ? null
            : (t instanceof String ? (String) t : JSON.toJSONString(t));

    /**
     * map val 智能转json
     */
    Function<Map<String, Object>, Map<String, String>> mapValSmartToJson = t ->
            t == null ? null : Maps.transformValues(t, v -> smartToJson.apply(v));

    /**
     * map val 智能转json
     */
    Function<Map<String, Object>, Map<String, String>> mapNullValRmSmartToJson = t -> {
        if (t == null) {
            return null;
        }
        Map<String, String> r = Maps.newHashMap();
        t.forEach((k, v) -> {
            if (v != null) {
                r.put(k, smartToJson.apply(v));
            }
        });
        return r;
    };

    /**
     * 切分字符串
     */
    BiFunction<String, String, Optional<Stream<String>>> stringSplit = (t, u) -> Optional.ofNullable(t)
            .map(p -> Arrays.stream(p.split(u)).map(String::trim).filter(StringUtils::isNotEmpty));

    /**
     * 切分字符串到List
     */
    BiFunction<String, String, List<String>> safeSplitToList =
            (t, u) -> Arrays.stream(Optional.ofNullable(t).orElse("").split(u))
                    .map(String::trim).filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());

    /**
     * 切分字符串到Set
     */
    BiFunction<String, String, Set<String>> safeSplitToSet = safeSplitToList.andThen(l -> Sets.newHashSet(l));

}
