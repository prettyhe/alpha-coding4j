package com.alpha.coding.bo.function.common;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;

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
            t == null ? null : t.entrySet().stream().collect(Collectors
                    .toMap(Map.Entry::getKey, a -> smartToJson.apply(a.getValue())));

    /**
     * map val 智能转json
     */
    Function<Map<String, Object>, Map<String, String>> mapNullValRmSmartToJson = t -> {
        if (t == null) {
            return null;
        }
        Map<String, String> r = new HashMap<>();
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
            .map(p -> Arrays.stream(p.split(u)).map(String::trim).filter(x -> !x.isEmpty()));

    /**
     * 切分字符串到List
     */
    BiFunction<String, String, Optional<List<String>>> stringSplitToList =
            (t, u) -> stringSplit.apply(t, u).map(p -> p.collect(Collectors.toList()));

    /**
     * 切分字符串到List
     */
    BiFunction<String, String, List<String>> stringSplitToListDefaultEmpty =
            (t, u) -> stringSplitToList.apply(t, u).orElse(Collections.emptyList());

    /**
     * 切分字符串到Set
     */
    BiFunction<String, String, Optional<Set<String>>> stringSplitToSet =
            (t, u) -> stringSplit.apply(t, u).map(p -> p.collect(Collectors.toSet()));

    /**
     * 切分字符串到Set
     */
    BiFunction<String, String, Set<String>> stringSplitToSetDefaultEmpty =
            (t, u) -> stringSplitToSet.apply(t, u).orElse(Collections.emptySet());

    /**
     * 切分字符串到整数List
     */
    BiFunction<String, String, Optional<List<Integer>>> strSplitToIntegerList =
            (t, u) -> stringSplit.apply(t, u)
                    .map(p -> p.filter(Predicates.isIntegerStr)
                            .map(Integer::valueOf).collect(Collectors.toList()));

    /**
     * 切分字符串到整数List
     */
    BiFunction<String, String, List<Integer>> strSplitToIntegerListDefaultEmpty =
            (t, u) -> strSplitToIntegerList.apply(t, u).orElse(Collections.emptyList());

    /**
     * 切分字符串到整数Set
     */
    BiFunction<String, String, Optional<LinkedHashSet<Integer>>> strSplitToIntegerSet =
            (t, u) -> stringSplit.apply(t, u)
                    .map(p -> p.filter(Predicates.isIntegerStr)
                            .map(Integer::valueOf).collect(Collectors.toCollection(LinkedHashSet::new)));

    /**
     * 切分字符串到整数Set
     */
    BiFunction<String, String, LinkedHashSet<Integer>> strSplitToIntegerSetDefaultEmpty =
            (t, u) -> strSplitToIntegerSet.apply(t, u).orElse(new LinkedHashSet<>(0));

    /**
     * 切分字符串到整数List
     */
    BiFunction<String, String, Optional<List<Long>>> strSplitToLongList =
            (t, u) -> stringSplit.apply(t, u)
                    .map(p -> p.filter(Predicates.isLongStr)
                            .map(Long::valueOf).collect(Collectors.toList()));

    /**
     * 切分字符串到整数List
     */
    BiFunction<String, String, List<Long>> strSplitToLongListDefaultEmpty =
            (t, u) -> strSplitToLongList.apply(t, u).orElse(Collections.emptyList());

    /**
     * 切分字符串到整数Set
     */
    BiFunction<String, String, Optional<LinkedHashSet<Long>>> strSplitToLongSet =
            (t, u) -> stringSplit.apply(t, u)
                    .map(p -> p.filter(Predicates.isLongStr)
                            .map(Long::valueOf).collect(Collectors.toCollection(LinkedHashSet::new)));

    /**
     * 切分字符串到整数Set
     */
    BiFunction<String, String, LinkedHashSet<Long>> strSplitToLongSetDefaultEmpty =
            (t, u) -> strSplitToLongSet.apply(t, u).orElse(new LinkedHashSet<>(0));

    /**
     * 时间单位
     */
    Function<TimeUnit, String> abbreviateTimeUnit = unit -> {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    };

    /**
     * 为纳秒数选择时间单位
     */
    Function<Long, TimeUnit> chooseUnitForNanos = nanos -> {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    };

    /**
     * 格式化4位小数
     */
    Function<Double, String> formatCompact4Digits = val -> String.format(Locale.ROOT, "%.4g", val);

    /**
     * 格式化纳秒数
     */
    Function<Long, String> formatNanos = nanos -> {
        TimeUnit unit = chooseUnitForNanos.apply(nanos);
        double value = (double) nanos / (double) TimeUnit.NANOSECONDS.convert(1L, unit);
        return formatCompact4Digits.apply(value) + " " + abbreviateTimeUnit.apply(unit);
    };

}
