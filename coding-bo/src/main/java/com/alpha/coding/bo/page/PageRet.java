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
 * PageRet
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PageRet implements Serializable {

    /**
     * 页码
     */
    private int pageNo;

    /**
     * 页大小
     */
    private int pageSize;

    /**
     * 总数
     */
    private long totalCount;

    /**
     * 排序条件(orderBy,order)
     */
    private List<Tuple<String, String>> orderBys;

    public PageRet(Page page) {
        if (page != null) {
            this.pageNo = page.getPageNo();
            this.pageSize = page.getPageSize();
            this.orderBys = page.getOrderBys();
        }
    }

    public PageRet(Page page, long totalCount) {
        this(page);
        this.totalCount = totalCount;
    }

    public PageRet(Integer pageNo, Integer pageSize, long totalCount) {
        this.pageNo = pageNo == null ? 0 : pageNo.intValue();
        this.pageSize = pageSize == null ? 0 : pageSize.intValue();
        this.totalCount = totalCount;
    }

}
