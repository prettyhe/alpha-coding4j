package com.alpha.coding.common.datasource;

/**
 * DataSourcePoolType
 *
 * @version 1.0
 * Date: 2023/4/25
 */
public enum DataSourceConnectionPoolType {

    /**
     * 自动，按照当前本地路径中的库查找
     */
    Auto,
    /**
     * Druid
     */
    Druid,
    /**
     * C3P0
     */
    C3P0,
    /**
     * HikariCP
     */
    HikariCP

}
