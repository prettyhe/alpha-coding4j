package com.alpha.coding.common.mybatis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
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

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.mybatis.common.DbType;
import com.alpha.coding.common.mybatis.common.MybatisParameterConvertor;
import com.alpha.coding.common.mybatis.common.TableNameParser;
import com.alpha.coding.common.utils.PropertiesUtils;
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

    private static final String CONF_SQL_ID_ABBR = "sqlIdAbbreviated";
    private static final String ENABLE_SHOW_DATABASE_NAME = "enableShowDatabaseName";
    private static final String THREAD_LOCAL_LEAK_CHECK_THRESHOLD = "threadLocalLeakCheckThreshold";
    private static final String DB_TYPE = "dbType";

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
        private static final ThreadLocal<Integer> VERSION_LOCAL = new ThreadLocal<>();
        /**
         * 版本对应的值
         */
        private static final ThreadLocal<Map<Integer, Map<String, Object>>> VERSION_DATA_LOCAL =
                ThreadLocal.withInitial(LinkedHashMap::new);

        /**
         * 获取当前版本号，非正数时为空，触发一次清理
         *
         * @return 当前版本号
         */
        public static Integer currentVersion() {
            Integer version = VERSION_LOCAL.get();
            if (version == null || version <= 0) {
                VERSION_DATA_LOCAL.remove();
                VERSION_LOCAL.remove();
                return null;
            }
            return version;
        }

        /**
         * 当前所有版本数据
         *
         * @return 所有版本数据
         */
        public static Map<Integer, Map<String, Object>> allVersionData() {
            return VERSION_DATA_LOCAL.get();
        }

        /**
         * 版本号增加，适用于最外层方法起始
         *
         * @return 操作之后的当前版本
         */
        public static Integer incrVersion() {
            Integer version = currentVersion();
            if (version == null) {
                version = 0;
            }
            version++;
            VERSION_LOCAL.set(version);
            return version;
        }

        /**
         * 版本号减小，适用于最外层方法结束
         *
         * @return 操作之后的当前版本
         */
        public static Integer decrVersion() {
            Integer version = currentVersion();
            if (version == null) {
                return null;
            }
            Map<Integer, Map<String, Object>> map = allVersionData();
            if (map != null) {
                map.remove(version);
            }
            version--;
            VERSION_LOCAL.set(version);
            return currentVersion();
        }

        /**
         * 获取当前版本数据，版本非空时才有
         */
        public static Map<String, Object> currentVersionDataMap() {
            Integer version = currentVersion();
            if (version == null) {
                return null;
            }
            Map<Integer, Map<String, Object>> map = allVersionData();
            if (map == null) {
                map = new LinkedHashMap<>();
                VERSION_DATA_LOCAL.set(map);
            }
            return map.computeIfAbsent(version, k -> new LinkedHashMap<>());
        }

        /**
         * 更新版本数据
         */
        public static void put(String key, Object val) {
            if (key == null) {
                throw new IllegalArgumentException("key cannot be null");
            }
            final Map<String, Object> map = currentVersionDataMap();
            if (map == null) {
                throw new UnsupportedOperationException("请先调用incrVersion初始化版本号");
            }
            map.put(key, val);
        }

        /**
         * 取出版本数据
         */
        public static Object get(String key) {
            final Map<String, Object> map = currentVersionDataMap();
            if (map != null) {
                return map.get(key);
            }
            return null;
        }

        /**
         * 移除版本数据
         */
        public static void remove(String key) {
            Map<String, Object> map = currentVersionDataMap();
            if (map != null) {
                map.remove(key);
            }
        }

        /**
         * 清空所有数据
         */
        public static void clearAll() {
            VERSION_DATA_LOCAL.remove();
            VERSION_LOCAL.remove();
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
        boolean isOutermost = false;
        try {
            if (MapThreadLocal.currentVersion() == null) {
                isOutermost = true; // 最外层调用
                MapThreadLocal.incrVersion(); // 初始版本
            }
            return doIntercept(invocation);
        } finally {
            // 检测version值，默认超过10则怀疑是ThreadLocal内存泄漏
            final String threshold = PropertiesUtils.getProperty(this.properties,
                    THREAD_LOCAL_LEAK_CHECK_THRESHOLD, "10");
            Integer version;
            if (StringUtils.isNumeric(threshold)
                    && (version = MapThreadLocal.currentVersion()) != null
                    && version >= Integer.parseInt(threshold)) {
                log.warn("Possible ThreadLocal leak detected for version {}>={}, please check！", version, threshold);
            }
            if (isOutermost) {
                try {
                    // 强制清理，无视当前版本号
                    MapThreadLocal.clearAll();
                } catch (Exception e) {
                    log.warn("ThreadLocal cleanup failed", e);
                }
            }
        }
    }

    private Object doIntercept(Invocation invocation) throws Throwable {
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
                log.warn("execute-sql {} fail, error: {}:{}", sqlId,
                        throwable.getClass().getName(), throwable.getMessage());
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
                                (Object[]) MapThreadLocal.get(sqlId + "_ParamsAfterSet"),
                                (DbType) MapThreadLocal.get(sqlId + "_DbType"));
                    }
                    if (sql != null) {
                        String sqlResultStr = Optional.ofNullable(SqlUtils.formatSQLExecResult(returnValue))
                                .map(s -> "; result: " + s).orElse("");
                        log.info("{} cost {}ms{}", sql, time, sqlResultStr);
                    }
                } catch (Throwable e) {
                    log.warn("parse sql from Executor fail for {}, error is {}:{}",
                            Optional.ofNullable(boundSql).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getClass().getName(), e.getMessage());
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
                    MapThreadLocal.put(sqlId + "_DbType", resolveDbType((Connection) invocation.getArgs()[0]));
                } catch (Throwable e) {
                    log.warn("resolve Configuration and BoundSql from StatementHandler fail for {}, error is {}:{}",
                            Optional.ofNullable(statementHandler.getBoundSql()).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getClass().getName(), e.getMessage());
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
                        final Object[] params = invocationHandler.resolveParams();
                        MapThreadLocal.put(sqlId + "_ParamsAfterSet", params);
                    }
                } catch (Throwable e) {
                    log.warn("resolve parameterValues from ParameterHandler fail for {}, error is {}:{}",
                            Optional.ofNullable(boundSql).map(BoundSql::getSql)
                                    .map(s -> s.replaceAll("\\s+", " ")).orElse(null),
                            e.getClass().getName(), e.getMessage());
                } finally {
                    invocationHandler.clear();
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
        private final List<Tuple<Integer, Object>> paramsAfterSet = new ArrayList<>(64);

        public ParamHolderPreparedStatementInvocationHandler(PreparedStatement ps) {
            this.target = ps;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.startsWith("set") && args.length >= 2 && args[0] instanceof Integer) {
                if (name.equals("setNull")) {
                    paramsAfterSet.add(Tuple.of((Integer) args[0] - 1, null));
                } else {
                    paramsAfterSet.add(Tuple.of((Integer) args[0] - 1, args[1]));
                }
            }
            return method.invoke(target, args);
        }

        public Object[] resolveParams() {
            Object[] params = new Object[paramsAfterSet.size()];
            paramsAfterSet.sort(Comparator.comparing(Tuple::getF));
            paramsAfterSet.forEach(p -> params[p.getF()] = p.getS());
            return params;
        }

        public void clear() {
            paramsAfterSet.clear();
        }

    }

    public String getSql(Configuration configuration, BoundSql boundSql, String sqlId,
                         String databaseName, Object[] params, DbType dbType) {
        try {
            String sql = showSql(configuration, boundSql, databaseName, params, dbType);
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

    private Object getParameterValue(Object obj) {
        Object target = obj;
        if (parameterConvertor != null) {
            target = parameterConvertor.convert(obj);
        }
        return target;
    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        return showSql(configuration, boundSql, null, null, null);
    }

    public String showSql(Configuration configuration, BoundSql boundSql, String databaseName,
                          Object[] params, DbType dbType) {
        final Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        sql = joinDatabaseName(sql, databaseName);
        if (parameterMappings != null && !parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", SqlUtils.formatValueToSQLString(getParameterValue(parameterObject)));
            } else {
                Object[] objects = new Object[parameterMappings.size()];
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (int i = 0; i < parameterMappings.size(); i++) {
                    // 优先取set到 PreparedStatement 中的值进行打印
                    objects[i] = params == null || params.length <= i ? null : params[i];
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
                            log.debug("getParameterValue failed for {}", propertyName, e);
                        }
                    }
                }
                // assemble final sql
                // sql = SqlUtils.printSQL(sql, objects);
                sql = SqlUtils.replacePlaceholders(sql, objects, dbType);
            }
        }
        return sql;
    }

    private boolean enableAbbreviateSqlId() {
        final String prop = PropertiesUtils.getProperty(this.properties, CONF_SQL_ID_ABBR, null);
        return prop != null && Boolean.parseBoolean(prop.trim());
    }

    private boolean enableShowDatabaseName() {
        final String prop = PropertiesUtils.getProperty(this.properties, ENABLE_SHOW_DATABASE_NAME, null);
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

    private DbType resolveDbType(Connection connection) {
        DbType dbType = null;
        // 优先从配置中获取
        final String property = PropertiesUtils.getProperty(this.properties, DB_TYPE, null);
        if (StringUtils.isNotBlank(property)) {
            dbType = DbType.resolveDbType(property);
        }
        // 其次从JDBC的url中获取
        if (dbType == null) {
            try {
                dbType = DbType.resolveDbType(connection.getMetaData().getURL());
            } catch (Exception e) {
                log.warn("resolve database type by Connection.getMetaData() fail, msg is: {}", e.getMessage());
            }
        }
        return dbType;
    }

}
