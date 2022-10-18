package com.alpha.coding.bo.page;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Pageable
 *
 * @version 1.0
 * Date: 2020-01-15
 */
@Data
@Accessors(chain = true)
public abstract class Pageable<T> implements Serializable {

    private Integer pageNo;
    private Integer pageSize;
    private T lastId;
    private String orderBy;

    private volatile transient Integer offset;
    private volatile transient Integer limit;

    public abstract String parseOrderBy();

    @SuppressWarnings({"rawtypes"})
    public Pageable refreshOffsetAndLimit() {
        if (this.pageNo == null || this.pageSize == null) {
            return this;
        }
        this.offset = (this.pageNo - 1) * this.pageSize;
        this.limit = this.pageSize;
        return this;
    }

    public Integer refreshOffset() {
        if (this.pageNo != null && this.pageSize != null) {
            this.offset = (this.pageNo - 1) * this.pageSize;
        }
        return this.offset;
    }

    public Integer refreshLimit() {
        this.limit = this.pageSize;
        return this.limit;
    }

}
