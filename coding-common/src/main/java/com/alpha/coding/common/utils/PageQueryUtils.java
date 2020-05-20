package com.alpha.coding.common.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.select.CountDSL;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.mybatis.dynamic.sql.select.SelectModel;
import org.mybatis.dynamic.sql.where.AbstractWhereDSL;

import com.alpha.coding.bo.page.PageRet;
import com.alpha.coding.bo.response.PageResData;

/**
 * PageQueryUtils
 *
 * @version 1.0
 * Date: 2020/4/11
 */
public class PageQueryUtils {

    /**
     * pageQuery 分页查询
     *
     * @param pageNo              页码
     * @param pageSize            页大小
     * @param countSup            总数提供函数
     * @param orderByRef          排序变量引用，为长度为1的数组，元素为排序变量
     * @param orderByPostConsumer 排序变量后置消费函数
     * @param selectListSup       查询列表提供函数
     * @param mapper              PO->VO函数
     * @return PageResData
     */
    public static <P, T> PageResData<T> pageQuery(Integer pageNo, Integer pageSize, Supplier<Long> countSup,
                                                  String[] orderByRef, Consumer<String> orderByPostConsumer,
                                                  Supplier<List<P>> selectListSup,
                                                  Function<? super P, ? extends T> mapper) {
        final PageRet pageRet = new PageRet();
        if (pageNo != null && pageSize != null) {
            pageRet.setPageNo(pageNo).setPageSize(pageSize);
            final long count = countSup.get();
            pageRet.setTotalCount(count);
            if (count <= 0) {
                return new PageResData<>(pageRet, null);
            }
            orderByRef[0] = orderByRef[0] + " limit " + (pageSize * (pageNo - 1)) + "," + pageSize;
        }
        if (orderByPostConsumer != null) {
            orderByPostConsumer.accept(orderByRef[0]);
        }
        final List<T> vos = selectListSup.get().stream().map(mapper).collect(Collectors.toList());
        return new PageResData<>(pageRet, vos);
    }

    /**
     * pageQuery 分页查询
     *
     * @param pageNo           页码
     * @param pageSize         页大小
     * @param countDSLSup      总数查询DSL提供函数
     * @param queryDSLSup      列表查询DSL提供函数
     * @param whereDSLConsumer where语句补充函数
     * @param countFunc        总数查询函数
     * @param orderByFunc      排序函数
     * @param selectListFunc   列表获取函数
     * @param mapper           PO->VO函数
     * @return PageResData
     */
    public static <P, T> PageResData<T> pageQuery(Integer pageNo, Integer pageSize,
                                                  Supplier<CountDSL<SelectModel>.CountWhereBuilder> countDSLSup,
                                                  Supplier<QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder> queryDSLSup,
                                                  Consumer<AbstractWhereDSL> whereDSLConsumer,
                                                  Function<CountDSL<SelectModel>.CountWhereBuilder, Long> countFunc,
                                                  Consumer<QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder> orderByFunc,
                                                  Function<QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder, List<P>> selectListFunc,
                                                  Function<? super P, ? extends T> mapper) {
        final PageRet pageRet = new PageRet();
        if (pageNo != null && pageSize != null) {
            pageRet.setPageNo(pageNo).setPageSize(pageSize);
            final CountDSL<SelectModel>.CountWhereBuilder countDSL = countDSLSup.get();
            if (whereDSLConsumer != null) {
                whereDSLConsumer.accept(countDSL);
            }
            final long count = countFunc.apply(countDSL);
            pageRet.setTotalCount(count);
            if (count <= 0) {
                return new PageResData<>(pageRet, null);
            }
        }
        final QueryExpressionDSL<SelectModel>.QueryExpressionWhereBuilder queryDSL = queryDSLSup.get();
        if (whereDSLConsumer != null) {
            whereDSLConsumer.accept(queryDSL);
        }
        if (orderByFunc != null) {
            orderByFunc.accept(queryDSL);
        }
        if (pageNo != null && pageSize != null) {
            queryDSL.limit(pageSize).offset((pageNo - 1) * pageSize);
        }
        final List<T> vos = selectListFunc.apply(queryDSL).stream().map(mapper).collect(Collectors.toList());
        return new PageResData<>(pageRet, vos);
    }

    /**
     * pageQuery 分页查询
     *
     * @param pageNo         页码
     * @param pageSize       页大小
     * @param countFunc      查询总数提供函数
     * @param selectListFunc 查询列表提供函数
     * @param mapper         PO->VO
     * @return PageResData
     */
    public static <P, T> PageResData<T> pageQuery(Integer pageNo, Integer pageSize,
                                                  Supplier<Long> countFunc,
                                                  Supplier<List<P>> selectListFunc,
                                                  Function<? super P, ? extends T> mapper) {
        final PageRet pageRet = new PageRet();
        if (pageNo != null && pageSize != null) {
            pageRet.setPageNo(pageNo).setPageSize(pageSize);
            final Long totalCount = countFunc.get();
            pageRet.setTotalCount(totalCount);
            if (totalCount <= 0) {
                return new PageResData<>(pageRet, null);
            }
        }
        final List<T> vos = selectListFunc.get().stream().map(mapper).collect(Collectors.toList());
        return new PageResData<>(pageRet, vos);
    }

}
