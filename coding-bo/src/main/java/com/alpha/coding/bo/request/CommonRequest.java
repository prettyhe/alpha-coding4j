package com.alpha.coding.bo.request;

import java.io.Serializable;

import javax.validation.Valid;

import com.alpha.coding.bo.page.Page;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * CommonRequest
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class CommonRequest<P, T> extends BaseRequest implements Serializable {

    /**
     * 分页参数，optional
     */
    private Page<P> page;

    /**
     * 数据
     */
    @Valid
    private T dataModel;

}
