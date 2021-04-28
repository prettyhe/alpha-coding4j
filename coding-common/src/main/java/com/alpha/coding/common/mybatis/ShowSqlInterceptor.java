/**
 * Copyright
 */
package com.alpha.coding.common.mybatis;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import com.alpha.coding.common.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * ShowSqlInterceptor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class ShowSqlInterceptor implements Interceptor {

    private static final String PLACEHOLDER = "%s";
    private static final String PLACEHOLDER_FOR_SIGN = "";
    private static final String CONF_SQL_ID_ABBR = "sqlIdAbbreviated";
    private static final String SEP_DOT = "\\.";
    private static final String DOT = ".";

    private Properties properties;

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        String sqlId = mappedStatement.getId();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        Object returnValue = null;
        long start = System.currentTimeMillis();
        try {
            returnValue = invocation.proceed();
        } catch (Throwable throwable) {
            log.warn("execute-sql {} fail, msg: {}", sqlId, throwable.getMessage());
            throw throwable;
        } finally {
            long end = System.currentTimeMillis();
            long time = end - start;
            String sql = getSql(configuration, boundSql, sqlId, time);
            if (sql != null) {
                log.info(sql);
            }
        }
        return returnValue;
    }

    public String getSql(Configuration configuration, BoundSql boundSql, String sqlId, long time) {
        try {
            String sql = showSql(configuration, boundSql);
            StringBuilder str = new StringBuilder(100);
            str.append(enableAbbreviateSqlId() ? abbreviateSqlId(sqlId) : sqlId);
            str.append(": ");
            str.append(sql);
            str.append("; cost ");
            str.append(time);
            str.append("ms");
            return str.toString();
        } catch (Throwable t) {
            String originSql = null;
            try {
                originSql = boundSql.getSql().replaceAll("[\\s]+", " ");
            } catch (Exception e) {
                // nothing
            }
            log.warn("parse-sql {} fail, origin sql: {}", sqlId, originSql, t);
        }
        return null;
    }

    private String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            value = "'" + DateUtils.format((Date) obj) + "'";
        } else if (obj instanceof byte[]) {
            value = "''";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                /**
                 * 注意，null转化成mysql的NULL
                 */
                value = "NULL";
            }
        }
        return value;
    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                Object[] objects = new Object[parameterMappings.size()];
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                int i = 0;
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    try {
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            objects[i++] = getParameterValue(obj);
                            continue;
                        }
                    } catch (Exception e) {
                        log.debug("getParameterValue for {}", propertyName, e);
                    }
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        objects[i++] = getParameterValue(obj);
                    }
                }
                // assemble final sql
                if (sql.contains("%")) {
                    String[] strs = sql.split("%", -1);
                    int j = 0;
                    for (int k = 0; k < strs.length; k++) {
                        while (strs[k].contains("?")) {
                            strs[k] = strs[k].replaceFirst("\\?", PLACEHOLDER);
                            strs[k] = String.format(strs[k], objects[j++]);
                        }
                    }
                    sql = StringUtils.join(strs, "%");
                } else {
                    sql = sql.replaceAll("\\?", PLACEHOLDER);
                    sql = String.format(sql, objects);
                }
            }
        }
        return sql;
    }

    /**
     * 此种实现略有问题，如果先遇到的字符串中有?则会替换掉
     */
    @Deprecated
    public String showSqlV1(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties0) {
        this.properties = properties0;
    }

    private String abbreviateSqlId(String sqlId) {
        if (sqlId == null) {
            return sqlId;
        }
        try {
            final String[] arr = sqlId.split(SEP_DOT);
            if (arr.length <= 2) {
                return sqlId;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length - 2; i++) {
                sb.append(arr[i].substring(0, 1)).append(DOT);
            }
            sb.append(arr[arr.length - 2]).append(DOT).append(arr[arr.length - 1]);
            return sb.toString();
        } catch (Exception e) {
            log.debug("abbreviate sqlId fail, {}", e.getMessage());
            return sqlId;
        }
    }

    private String getProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        if (this.properties.getProperty(key) != null) {
            return this.properties.getProperty(key);
        }
        if (this.properties.get(key) != null) {
            return String.valueOf(this.properties.get(key));
        }
        return null;
    }

    private boolean enableAbbreviateSqlId() {
        final String prop = getProperty(CONF_SQL_ID_ABBR);
        return prop != null && Boolean.parseBoolean(prop.trim());
    }

}
