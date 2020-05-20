package com.alpha.coding.common.http.rest;

import java.util.List;

/**
 * UriSelectPolicy
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface UriSelectPolicy {

    /**
     * 选择
     */
    String select(List<String> uriList);

}
