package com.alpha.coding.common.mybatis;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import com.alpha.coding.common.mybatis.common.MybatisParameterConvertor;
import com.alpha.coding.common.mybatis.common.TableNameParser;
import com.alpha.coding.common.utils.SqlUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

;

/**
 * ShowSqlInterceptor
 *
 * @author hejiangshan on 2017年9月12日
 * @version 1.0
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class}),
        @Signature(type = StatementHandler.class, method = "prepare",
                args = {Connection.class, Integer.class})
})
public class ShowSqlInterceptor implements Interceptor {

    private static final String PLACEHOLDER = "%s";
    private static final String CONF_SQL_ID_ABBR = "sqlIdAbbreviated";
    private static final String ENABLE_SHOW_DATABASE_NAME = "enableShowDatabaseName";

    /**
     * 属性配置
     */
    @Setter
    private Properties properties;

    @Setter
    private MybatisParameterConvertor parameterConvertor;

    /**
     * 静态内部类工具
     */
    private static final class MapThreadLocal {
        private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL =
                new InheritableThreadLocal<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> childValue(Map<String, Object> parentValue) {
                        if (parentValue == null) {
                            return null;
                        }
                        return new LinkedHashMap<>(parentValue);
                    }
                };

        public static void put(String key, Object val) {
            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map == null) {
                synchronized(MapThreadLocal.class) {
                    map = THREAD_LOCAL.get();
                    if (map == null) {
                        map = new HashMap<>();
                        THREAD_LOCAL.set(map);
                    }
                }
            }
            map.put(key, val);
        }

        public static Object get(String key) {
            Map<String, Object> map = THREAD_LOCAL.get();
            if ((map != null) && (key != null)) {
                return map.get(key);
            } else {
                return null;
            }
        }

        public static void remove(String key) {
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map != null) {
                map.remove(key);
            }
        }

        public static void clear() {
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map != null) {
                map.clear();
                THREAD_LOCAL.remove();
            }
        }

        public static Set<String> getKeys() {
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map != null) {
                return map.keySet();
            } else {
                return null;
            }
        }

        public static Map<String, Object> getCopyOfContextMap() {
            Map<String, Object> oldMap = THREAD_LOCAL.get();
            if (oldMap != null) {
                return new LinkedHashMap<>(oldMap);
            } else {
                return null;
            }
        }

        public static <V> V computeIfAbsent(String key, Function<String, ? extends V> mappingFunction) {
            Map<String, Object> map = THREAD_LOCAL.get();
            if (map == null) {
                synchronized(MapThreadLocal.class) {
                    map = THREAD_LOCAL.get();
                    if (map == null) {
                        map = new HashMap<>();
                        THREAD_LOCAL.set(map);
                    }
                }
            }
            return (V) map.computeIfAbsent(key, mappingFunction);
        }

    }

    /**
     * 获取代理对象的实际对象
     */
    private static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        } else {
            return (T) target;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object target = invocation.getTarget();
        final boolean enableShowDatabaseName = enableShowDatabaseName();
        if (target instanceof Executor) {
            final Object[] invocationArgs = invocation.getArgs();
            final MappedStatement mappedStatement = (MappedStatement) invocationArgs[0];
            final String sqlId = mappedStatement.getId();
            final long start = System.currentTimeMillis();
            Object returnValue = null;
            try {
                returnValue = invocation.proceed();
                return returnValue;
            } catch (Throwable throwable) {
                log.warn("execute-sql {} fail, msg: {}", sqlId, throwable.getMessage());
                throw throwable;
            } finally {
                BoundSql boundSql = null;
                try {
                    final long end = System.currentTimeMillis();
                    final long time = end - start;
                    Object sql = MapThreadLocal.get(sqlId);
                    if (sql == null) {
                        Object parameter = null;
                        if (invocationArgs.length > 1) {
                            parameter = invocationArgs[1];
                        }
                        if (invocationArgs.length == 6 && invocationArgs[5] instanceof BoundSql) {
                            boundSql = (BoundSql) invocationArgs[5];
                        } else {
                            boundSql = mappedStatement.getBoundSql(parameter);
                        }
                        Configuration configuration = mappedStatement.getConfiguration();
                        sql = getSql(configuration, boundSql, sqlId, null);
                    }
                    if (sql != null) {
                        String sqlResultStr = Optional.ofNullable(SqlUtils.formatSQLExecResult(returnValue))
                                .map(s -> "; result: " + s).orElse("");
                        log.info("{} cost {}ms{}", sql, time, sqlResultStr);
                    }
                    MapThreadLocal.remove(sqlId);
                } catch (Throwable e) {
                    log.warn("parse sql from StatementHandler fail for {}, msg is {}",
                            Optional.ofNullable(boundSql).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getMessage());
                }
            }
        } else if (target instanceof StatementHandler) {
            final StatementHandler statementHandler = (StatementHandler) target;
            Object returnValue = null;
            try {
                returnValue = invocation.proceed();
                return returnValue;
            } finally {
                try {
                    String databaseName = null;
                    if (enableShowDatabaseName) {
                        databaseName = resolveDatabaseName((Connection) invocation.getArgs()[0]);
                    }
                    final MetaObject object = SystemMetaObject.forObject(realTarget(statementHandler));
                    final MybatisStatementHandler handler =
                            new MybatisStatementHandler(SystemMetaObject.forObject(object.getValue("delegate")));
                    final MappedStatement mappedStatement = handler.mappedStatement();
                    final String sqlId = mappedStatement.getId();
                    final String sql = getSql(handler.configuration(), handler.boundSql(),
                            mappedStatement.getId(), databaseName);
                    MapThreadLocal.put(sqlId, sql);
                } catch (Throwable e) {
                    log.warn("parse sql from StatementHandler fail for {}, msg is {}",
                            Optional.ofNullable(statementHandler.getBoundSql()).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getMessage());
                }
            }
        } else {
            return invocation.proceed();
        }
    }

    public String getSql(Configuration configuration, BoundSql boundSql, String sqlId, String databaseName) {
        try {
            String sql = showSql(configuration, boundSql, databaseName);
            StringBuilder str = new StringBuilder(100);
            str.append(enableAbbreviateSqlId() ? StringUtils.abbreviateDotSplit(sqlId, 1) : sqlId);
            str.append(": ");
            str.append(sql);
            str.append(";");
            return str.toString();
        } catch (Throwable t) {
            String originSql = null;
            try {
                originSql = boundSql.getSql().replaceAll("\\s+", " ");
            } catch (Exception e) {
                // nothing
            }
            log.warn("parse-sql {} fail, origin sql: {}", sqlId, originSql, t);
        }
        return null;
    }

    private String getParameterValue(Object obj) {
        Object target = obj;
        if (parameterConvertor != null) {
            target = parameterConvertor.convert(obj);
        }
        return SqlUtils.formatValueToSQLString(target);
    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        return showSql(configuration, boundSql, null);
    }

    public String showSql(Configuration configuration, BoundSql boundSql, String databaseName) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        sql = joinDatabaseName(sql, databaseName);
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                Object[] objects = new Object[parameterMappings.size()];
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (int i = 0; i < parameterMappings.size(); i++) {
                    String propertyName = parameterMappings.get(i).getProperty();
                    try {
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            objects[i] = getParameterValue(obj);
                            continue;
                        }
                        if (boundSql.hasAdditionalParameter(propertyName)) {
                            Object obj = boundSql.getAdditionalParameter(propertyName);
                            objects[i] = getParameterValue(obj);
                            continue;
                        }
                        Object obj = metaObject.getValue(propertyName);
                        objects[i] = getParameterValue(obj);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("getParameterValue for {}", propertyName, e);
                        }
                    }
                }
                // assemble final sql
                if (sql.contains("%")) {
                    String[] tokens = sql.split("%", -1);
                    int j = 0;
                    for (int k = 0; k < tokens.length; k++) {
                        while (tokens[k].contains("?")) {
                            tokens[k] = tokens[k].replaceFirst("\\?", PLACEHOLDER);
                            tokens[k] = String.format(tokens[k], objects[j++]);
                        }
                    }
                    sql = StringUtils.join(tokens, "%");
                } else {
                    sql = sql.replaceAll("\\?", PLACEHOLDER);
                    sql = String.format(sql, objects);
                }
            }
        }
        return sql;
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

    private boolean enableShowDatabaseName() {
        final String prop = getProperty(ENABLE_SHOW_DATABASE_NAME);
        return prop != null && Boolean.parseBoolean(prop.trim());
    }

    /**
     * 处理SQL，拼接库名. 默认拼接形式为 ${库名}.${表名}
     */
    private String joinDatabaseName(String sql, String databaseName) {
        if (databaseName == null) {
            return sql;
        }
        try {
            TableNameParser parser = new TableNameParser(sql);
            List<TableNameParser.SqlToken> names = new ArrayList<>();
            parser.accept(names::add);
            StringBuilder builder = new StringBuilder();
            int last = 0;
            for (TableNameParser.SqlToken name : names) {
                int start = name.getStart();
                if (start != last) {
                    builder.append(sql, last, start);
                    final String tableName = name.getValue();
                    if (tableName.contains(".")) {
                        builder.append(tableName);
                    } else {
                        builder.append(databaseName).append(".").append(tableName);
                    }
                }
                last = name.getEnd();
            }
            if (last != sql.length()) {
                builder.append(sql.substring(last));
            }
            return builder.toString();
        } catch (Exception e) {
            log.warn("joinDatabaseName fail for {} fail,  msg is: {}", sql, e.getMessage());
        }
        return sql;
    }

    /**
     * 从数据库连接中拿当前库名
     */
    private String resolveDatabaseName(Connection connection) {
        if (connection == null) {
            return null;
        }
        String databaseName = null;
        try {
            // MySQL, PostgreSQL, Oracle 等通常使用 getCatalog()
            databaseName = connection.getCatalog();
        } catch (SQLException e) {
            log.warn("resolve database name by Connection.getCatalog() fail, msg is: {}", e.getMessage());
        }
        if (databaseName == null) {
            try {
                // SQL Server, DB2 等可能会使用 getSchema()
                databaseName = connection.getSchema();
            } catch (SQLException e) {
                log.warn("resolve database name by Connection.getSchema() fail, msg is: {}", e.getMessage());
            }
        }
        return databaseName;
    }

}
