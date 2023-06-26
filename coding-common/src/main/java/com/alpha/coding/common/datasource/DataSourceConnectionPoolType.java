package com.alpha.coding.common.datasource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DataSourcePoolType
 *
 * @version 1.0
 * Date: 2023/4/25
 */
@Getter
@AllArgsConstructor
public enum DataSourceConnectionPoolType {

    /**
     * 自动，按照当前本地路径中的库查找
     */
    Auto("Auto", ""),
    /**
     * Druid
     */
    Druid("Druid", "com.alibaba.druid.pool.DruidDataSource"),
    /**
     * C3P0
     */
    C3P0("C3P0", "com.mchange.v2.c3p0.ComboPooledDataSource"),
    /**
     * HikariCP
     */
    HikariCP("HikariCP", "com.zaxxer.hikari.HikariDataSource");

    private final String type;
    private final String dataSourceClass;

}
