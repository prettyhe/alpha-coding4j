package com.alpha.coding.common.mybatis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * ShowSqlInterceptor
 *
 * @author js on 2017年9月12日
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
                args = {Connection.class, Integer.class}),
        @Signature(type = ParameterHandler.class, method = "setParameters",
                args = {PreparedStatement.class})
})
public class ShowSqlInterceptor implements Interceptor {

    private static final String PLACEHOLDER = "%s";
    private static final String CONF_SQL_ID_ABBR = "sqlIdAbbreviated";
    private static final String ENABLE_SHOW_DATABASE_NAME = "enableShowDatabaseName";

    /**
     * 属性配置
     */
    private Properties properties;

    @Setter
    private MybatisParameterConvertor parameterConvertor;

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * 静态内部类工具
     */
    private static final class MapThreadLocal {
        /**
         * 当前版本号
         */
        private static final InheritableThreadLocal<Integer> VERSION_LOCAL = new InheritableThreadLocal<>();
        /**
         * 版本对应的值
         */
        private static final InheritableThreadLocal<Map<Integer, Map<String, Object>>> THREAD_LOCAL =
                new InheritableThreadLocal<Map<Integer, Map<String, Object>>>() {
                    @Override
                    protected Map<Integer, Map<String, Object>> childValue(
                            Map<Integer, Map<String, Object>> parentValue) {
                        if (parentValue == null) {
                            return null;
                        }
                        return new LinkedHashMap<>(parentValue);
                    }
                };

        /**
         * 版本号增加，适用于最外层方法起始
         */
        public static void incrVersion() {
            Integer version = VERSION_LOCAL.get();
            if (version == null || version <= 0) {
                THREAD_LOCAL.remove();
                version = 0;
            }
            version++;
            VERSION_LOCAL.set(version);
        }

        /**
         * 版本号减小，适用于最外层方法结束
         */
        public static void decrVersion() {
            Integer version = VERSION_LOCAL.get();
            if (version != null) {
                version--;
            }
            if (version == null || version <= 0) {
                THREAD_LOCAL.remove();
                VERSION_LOCAL.remove();
            }
        }

        private static Map<String, Object> currentMap() {
            Integer version = VERSION_LOCAL.get();
            if (version == null || version == 0) {
                incrVersion();
            }
            Map<Integer, Map<String, Object>> map = THREAD_LOCAL.get();
            if (map == null) {
                synchronized(MapThreadLocal.class) {
                    map = THREAD_LOCAL.get();
                    if (map == null) {
                        map = new HashMap<>();
                        THREAD_LOCAL.set(map);
                    }
                }
            }
            return map.computeIfAbsent(version, k -> new LinkedHashMap<>());
        }

        public static void put(String key, Object val) {
            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }
            currentMap().put(key, val);
        }

        public static Object get(String key) {
            return currentMap().get(key);
        }

        public static void remove(String key) {
            currentMap().remove(key);
        }

    }

    /**
     * 获取代理对象的实际对象
     */
    private static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            Object h = metaObject.getValue("h");
            if (h == null) {
                return null;
            }
            MetaObject metaObjectForH = SystemMetaObject.forObject(h);
            if (metaObjectForH.hasGetter("target")) {
                return realTarget(metaObjectForH.getValue("target"));
            }
            return realTarget(h);
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
            MapThreadLocal.incrVersion(); // Executor是起始，进行初始化
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
                        Configuration configuration = mappedStatement.getConfiguration();
                        if (MapThreadLocal.get(sqlId + "_Configuration") != null) {
                            // 优先取 StatementHandler 处理时拿到的配置
                            configuration = (Configuration) MapThreadLocal.get(sqlId + "_Configuration");
                        }
                        if (MapThreadLocal.get(sqlId + "_BoundSql") != null) {
                            // 优先取 StatementHandler 处理时拿到的BoundSql
                            boundSql = (BoundSql) MapThreadLocal.get(sqlId + "_BoundSql");
                        } else if (invocationArgs.length == 6 && invocationArgs[5] instanceof BoundSql) {
                            boundSql = (BoundSql) invocationArgs[5];
                        } else {
                            boundSql = mappedStatement.getBoundSql(parameter);
                        }
                        sql = getSql(configuration, boundSql, sqlId,
                                (String) MapThreadLocal.get(sqlId + "_DatabaseName"),
                                (Map<Integer, Object>) MapThreadLocal.get(sqlId + "_ParamsAfterSet"));
                    }
                    if (sql != null) {
                        String sqlResultStr = Optional.ofNullable(SqlUtils.formatSQLExecResult(returnValue))
                                .map(s -> "; result: " + s).orElse("");
                        log.info("{} cost {}ms{}", sql, time, sqlResultStr);
                    }
                } catch (Throwable e) {
                    log.warn("parse sql from Executor fail for {}, msg is {}",
                            Optional.ofNullable(boundSql).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getMessage());
                } finally {
                    MapThreadLocal.decrVersion();
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
                    final MetaObject object = SystemMetaObject.forObject(realTarget(statementHandler));
                    final MybatisStatementHandler handler =
                            new MybatisStatementHandler(SystemMetaObject.forObject(object.getValue("delegate")));
                    final MappedStatement mappedStatement = handler.mappedStatement();
                    final String sqlId = mappedStatement.getId();
                    if (enableShowDatabaseName) {
                        String databaseName = resolveDatabaseName((Connection) invocation.getArgs()[0]);
                        MapThreadLocal.put(sqlId + "_DatabaseName", databaseName);
                    }
                    MapThreadLocal.put(sqlId + "_Configuration", handler.configuration());
                    MapThreadLocal.put(sqlId + "_BoundSql", handler.boundSql());
                } catch (Throwable e) {
                    log.warn("resolve Configuration and BoundSql from StatementHandler fail for {}, msg is {}",
                            Optional.ofNullable(statementHandler.getBoundSql()).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getMessage());
                }
            }
        } else if (target instanceof ParameterHandler) {
            final ParameterHandler parameterHandler = (ParameterHandler) target;
            final PreparedStatement preparedStatement = (PreparedStatement) invocation.getArgs()[0];
            final ParamHolderPreparedStatementInvocationHandler invocationHandler =
                    new ParamHolderPreparedStatementInvocationHandler(preparedStatement);
            final PreparedStatement preparedStatementProxy = (PreparedStatement) Proxy.newProxyInstance(
                    preparedStatement.getClass().getClassLoader(),
                    new Class[] {PreparedStatement.class}, invocationHandler);
            try {
                parameterHandler.setParameters(preparedStatementProxy);
                return null;
            } finally {
                BoundSql boundSql = null;
                try {
                    final MetaObject metaObject = SystemMetaObject.forObject(realTarget(parameterHandler));
                    final Object boundSqlValue =
                            metaObject.hasGetter("boundSql") ? metaObject.getValue("boundSql") : null;
                    if (boundSqlValue instanceof BoundSql) {
                        boundSql = (BoundSql) boundSqlValue;
                    }
                    final Object mappedStatementValue =
                            metaObject.hasGetter("mappedStatement") ? metaObject.getValue("mappedStatement") : null;
                    if (mappedStatementValue instanceof MappedStatement) {
                        final String sqlId = ((MappedStatement) mappedStatementValue).getId();
                        MapThreadLocal.put(sqlId + "_ParamsAfterSet", invocationHandler.getParamsAfterSet());
                    }
                } catch (Throwable e) {
                    log.warn("resolve parameterValues from ParameterHandler fail for {}, msg is {}",
                            Optional.ofNullable(boundSql).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getMessage());
                }
            }
        } else {
            return invocation.proceed();
        }
    }

    /**
     * 对PreparedStatement设置进去的值进行缓存的代理
     */
    public static class ParamHolderPreparedStatementInvocationHandler implements InvocationHandler {

        private final PreparedStatement target;
        @Getter
        private final Map<Integer, Object> paramsAfterSet = new LinkedHashMap<>();

        public ParamHolderPreparedStatementInvocationHandler(PreparedStatement ps) {
            this.target = ps;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.startsWith("set") && args.length >= 2 && args[0] instanceof Integer) {
                if (name.equals("setNull")) {
                    paramsAfterSet.put((Integer) args[0] - 1, null);
                } else {
                    paramsAfterSet.put((Integer) args[0] - 1, args[1]);
                }
            }
            return method.invoke(target, args);
        }

    }

    public String getSql(Configuration configuration, BoundSql boundSql, String sqlId, String databaseName,
                         Map<Integer, Object> paramsAfterSet) {
        try {
            String sql = showSql(configuration, boundSql, databaseName, paramsAfterSet);
            return (enableAbbreviateSqlId() ? StringUtils.abbreviateDotSplit(sqlId, 1) : sqlId)
                    + ": " + sql + ";";
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
        return showSql(configuration, boundSql, null, null);
    }

    public String showSql(Configuration configuration, BoundSql boundSql, String databaseName,
                          Map<Integer, Object> paramsAfterSet) {
        final Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        final Map<Integer, Object> paramIndexValueMap =
                Optional.ofNullable(paramsAfterSet).orElse(Collections.emptyMap());
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        sql = joinDatabaseName(sql, databaseName);
        if (parameterMappings != null && !parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                Object[] objects = new Object[parameterMappings.size()];
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (int i = 0; i < parameterMappings.size(); i++) {
                    // 优先取set到 PreparedStatement 中的值进行打印
                    objects[i] = paramIndexValueMap.get(i);
                    if (objects[i] != null) {
                        objects[i] = getParameterValue(objects[i]);
                        continue;
                    }
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
