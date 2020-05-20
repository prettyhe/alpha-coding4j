package com.alpha.coding.common.bean.fileinject.scan.filter;

/**
 * ScanFilter
 *
 * @version 1.0
 * Date: 2020-03-18
 */
public interface ScanFilter {

    /**
     * Determine whether this filter matches for the target.
     */
    boolean match(Object target, Object benchmark);

}
