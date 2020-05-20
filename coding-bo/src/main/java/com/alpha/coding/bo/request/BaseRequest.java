package com.alpha.coding.bo.request;

import java.io.Serializable;

import lombok.Data;

/**
 * BaseRequest
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
public class BaseRequest implements Serializable {

    /**
     * 请求id
     */
    private String requestId;

}
