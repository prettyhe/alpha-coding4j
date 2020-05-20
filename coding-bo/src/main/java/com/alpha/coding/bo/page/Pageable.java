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

    public abstract String parseOrderBy();

}
