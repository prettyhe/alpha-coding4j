/**
 * Copyright
 */
package com.alpha.coding.bo.page;

import java.io.Serializable;
import java.util.List;

import com.alpha.coding.bo.base.Tuple;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Page
 *
 * @param <T> id类型
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Page<T> implements Serializable {

    /**
     * 页码
     */
    private int pageNo = 1;

    /**
     * 页大小
     */
    private int pageSize = 10;

    /**
     * 排序条件(orderBy,order)
     */
    private List<Tuple<String, String>> orderBys;

    /**
     * 上一页的最后一个
     */
    private T lastId;

    public Page(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

}
