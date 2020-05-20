package com.alpha.coding.bo.response;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.alpha.coding.bo.page.PageRet;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * PageResData
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class PageResData<T> implements Serializable {

    private PageRet pageRet;
    private List<T> list;
    private Object extra;

    public PageResData() {
    }

    public PageResData(PageRet pageRet, List<T> list) {
        this.pageRet = pageRet;
        this.list = list;
    }

    public PageResData(PageRet pageRet, List<T> list, Object extra) {
        this.pageRet = pageRet;
        this.list = list;
        this.extra = extra;
    }

    public static PageResData emptyPage(PageRet pageRet) {
        return new PageResData(pageRet, Collections.emptyList());
    }

    public static PageResData emptyPage(PageRet pageRet, Object extra) {
        return new PageResData(pageRet, Collections.emptyList(), extra);
    }
}
