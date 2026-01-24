package com.alpha.coding.common.mybatis.common;

import com.alpha.coding.common.utils.StringUtils;

/**
 * DbType
 *
 * @version 1.0
 * @date 2025年12月24日
 */
public enum DbType {

    MYSQL, MARIADB, POSTGRESQL, ORACLE, DB2,
    SQL_SERVER, SQLITE, H2, OTHER;

    public static DbType resolveDbType(String typeOrJdbcUrl) {
        if (StringUtils.isBlank(typeOrJdbcUrl)) {
            throw new IllegalArgumentException("type Or JdbcUrl is null");
        }
        String rawType = typeOrJdbcUrl.toLowerCase();
        if (!rawType.startsWith("jdbc:")) {
            for (DbType value : DbType.values()) {
                if (value.name().equalsIgnoreCase(typeOrJdbcUrl)) {
                    return value;
                }
            }
        }
        if (rawType.contains(":mysql:") || rawType.contains(":cobar:")) {
            return DbType.MYSQL;
        } else if (rawType.contains(":mariadb:")) {
            return DbType.MARIADB;
        } else if (rawType.contains(":oracle:")) {
            return DbType.ORACLE;
        } else if (rawType.contains(":sqlserver:") || rawType.contains(":sqlserver2012:")) {
            return DbType.SQL_SERVER;
        } else if (rawType.contains(":microsoft:")) {
            return DbType.SQL_SERVER;
        } else if (rawType.contains(":postgresql:")) {
            return DbType.POSTGRESQL;
        } else if (rawType.contains(":db2:")) {
            return DbType.DB2;
        } else if (rawType.contains(":sqlite:")) {
            return DbType.SQLITE;
        } else if (rawType.contains(":h2:")) {
            return DbType.H2;
        } else {
            return null;
        }
    }

}
