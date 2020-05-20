/**
 * Copyright
 */
package com.alpha.coding.common.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * ListUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ListUtils {

    public static <T> List<T> toList(Collection<T> c) {
        if (c == null) {
            return null;
        }
        if (c instanceof List) {
            return (List<T>) c;
        }
        return Lists.newArrayList(c);
    }

    public static <T, K> List<T> convert(Collection<K> list, Class<T> clazz) {
        List<T> ret = Lists.newArrayList();
        if (CollectionUtils.isEmpty(list)) {
            return ret;
        }

        Converter converter = BeanUtilsBean.getInstance().getConvertUtils().lookup(clazz);
        if (converter == null) {
            return null;
        }
        for (K k : list) {
            T t = (T) converter.convert(clazz, k);
            ret.add(t);
        }
        return ret;
    }

    public static <T> List<T> getBatchList(List<T> list, int index, int batchSize) {
        int end = (index + batchSize) < list.size() ? (index + batchSize) : list.size();
        return list.subList(index, end);
    }

    public static <T> List<T> split(String str, char separatorChar, Class<T> clazz) {
        List<T> ret = Lists.newArrayList();
        if (StringUtils.isEmpty(str)) {
            return ret;
        }

        Converter converter = BeanUtilsBean.getInstance().getConvertUtils().lookup(clazz);
        if (converter == null) {
            return null;
        }
        String[] ts = StringUtils.split(str, separatorChar);
        for (String token : ts) {
            T t = (T) converter.convert(clazz, token);
            ret.add(t);
        }
        return ret;
    }

    public static <T> List<T> add(List<T> list, T item) {
        list.add(item);
        return list;
    }

    /**
     * 将长的list分解成短的list
     *
     * @param oriList 待分解的list
     * @param size    分解后的短list的长度
     */
    public static <T> List<List<T>> resolve(List<T> oriList, final int size) {
        List<List<T>> result = Lists.newArrayList();
        int totalSize = oriList.size();
        int n = (totalSize % size == 0) ? (totalSize / size) : (totalSize / size + 1);
        List<T> subList = Lists.newArrayList();
        for (int i = 1; i <= n; i++) {
            if (size * i <= totalSize) {
                subList = oriList.subList(size * (i - 1), size * i);
            } else {
                subList = oriList.subList(size * (i - 1), totalSize);
            }
            result.add(subList);
        }
        return result;
    }

    /**
     * List转成Array
     */
    public static <T> T[] toArray(List<T> list, Class<T> clazz) {
        if (list == null) {
            return null;
        }
        return list.toArray((T[]) Array.newInstance(clazz, 0));
    }

    public static <T> Set<T> toSet(List<T> list) {
        if (list == null) {
            return null;
        }
        Set<T> set = Sets.newHashSet();
        set.addAll(list);
        return set;
    }

    /**
     * 判断一个list(lb)是否是另一个list(la)的子集(元素顺序也需一致)，假设list中没有重复元素
     */
    public static <T> boolean contain(List<T> la, List<T> lb) {
        if (la == null && lb == null) {
            return true;
        }
        if ((la == null && lb != null) || (la != null && lb == null)) {
            return false;
        }
        if (la.size() < lb.size()) {
            return false;
        }
        for (int i = 0, j = 0; i < lb.size(); i++) {
            for (; j < la.size(); j++) {
                if (lb.get(i).equals(la.get(j))) {
                    break;
                }
            }
            if (j == la.size() && i < lb.size()) {
                return false;
            }
        }
        return true;
    }

    public static <T> List<T> safeSublist(List<T> list, int from, int size) {
        if (list == null || list.size() == 0) {
            return list;
        }
        if (from < 0 || size < 0) {
            throw new IllegalArgumentException("from=" + from + ",size=" + size);
        }
        from = list.size() >= from ? from : list.size();
        int to = (from + size) >= list.size() ? list.size() : from + size;
        return list.subList(from, to);
    }

    /**
     * 取子集
     * <p>如[1,2,3]</p>
     * <li>(0,1)=>[1]</li>
     * <li>(3,1)=>[]</li>
     * <li>(3,-1)=>[3]</li>
     * <li>(-1,-1)=>[2]</li>
     * <li>(-1,-4)=>[1,2]</li>
     * <li>(3,-4)=>[1,2,3]</li>
     *
     * @param list 原始list
     * @param from 起始位置，-1表示最后一个，-2表示倒数第二个
     * @param size 数目，-1表示往左截取一个，-2表示往左截取2个
     */
    public static <T> List<T> sublist(List<T> list, int from, int size) {
        if (list == null || list.size() == 0) {
            return list;
        }
        int start = 0;
        if (from >= 0) {
            start = list.size() >= from ? from : list.size();
        } else {
            start = list.size() + from >= 0 ? list.size() + from : 0;
        }
        int end = 0;
        if (size >= 0) {
            end = (start + size) >= list.size() ? list.size() : start + size;
        } else {
            end = start + size >= 0 ? start + size : 0;
        }
        return start <= end ? list.subList(start, end) : list.subList(end, start);
    }

    /**
     * <p>A+B</p>
     * 集合la与lb的并集
     */
    public static <T> List<T> add(List<T> la, List<T> lb) {
        List<T> ret = Lists.newArrayList();
        if (la != null) {
            ret.addAll(la);
        }
        if (lb != null) {
            ret.addAll(lb);
        }
        return ret;
    }

    /**
     * <p>A-B</p>
     * 集合la与lb的差集
     */
    public static <T> List<T> minus(List<T> la, List<T> lb, Function<T, Object> keyFunction) {
        if (CollectionUtils.isEmpty(la) || CollectionUtils.isEmpty(lb)) {
            return la;
        }
        Set<Object> setOfB = Sets.newHashSet();
        for (T t : lb) {
            setOfB.add(keyFunction.apply(t));
        }
        List<T> ret = Lists.newArrayList();
        for (T t : la) {
            if (!setOfB.contains(keyFunction.apply(t))) {
                ret.add(t);
            }
        }
        return ret;
    }

    /**
     * <p>A|B</p>
     * 集合la中也同时存在于集合lb中的
     */
    public static <T> List<T> aOfB(List<T> la, List<T> lb, Function<T, Object> keyFunction) {
        if (CollectionUtils.isEmpty(la) || CollectionUtils.isEmpty(lb)) {
            return Collections.emptyList();
        }
        Set<Object> setOfB = Sets.newHashSet();
        for (T t : lb) {
            setOfB.add(keyFunction.apply(t));
        }
        List<T> ret = Lists.newArrayList();
        for (T t : la) {
            if (setOfB.contains(keyFunction.apply(t))) {
                ret.add(t);
            }
        }
        return ret;
    }

}
