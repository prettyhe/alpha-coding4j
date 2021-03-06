package com.alpha.coding.common.mybatis;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

/**
 * DynamicPlugin
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class})})
public class DynamicPlugin implements Interceptor {

    private static final String FORCE_USE_WRITE_DATA_SOURCE_SQL = "forceUseWriteDataSourceSql";

    private static final String REGEX = ".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";

    private static final Map<String, DynamicDataSourceGlobal> CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * custom Properties
     */
    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if (!synchronizationActive) {
            Object[] objects = invocation.getArgs();
            MappedStatement ms = (MappedStatement) objects[0];

            DynamicDataSourceGlobal dynamicDataSourceGlobal = null;

            if ((dynamicDataSourceGlobal = CACHE_MAP.get(ms.getId())) == null) {
                // 是否强制使用写数据源
                final String[] forceUseWriteDataSourceSql = getForceUseWriteDataSourceSql();
                if (forceUseWriteDataSourceSql != null) {
                    for (String sql : forceUseWriteDataSourceSql) {
                        if (ms.getId().matches(sql)) {
                            dynamicDataSourceGlobal = DynamicDataSourceGlobal.WRITE;
                            break;
                        }
                    }
                }
                if (dynamicDataSourceGlobal != DynamicDataSourceGlobal.WRITE) {
                    // 读方法
                    if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                        // !selectKey 为自增id查询主键(SELECT LAST_INSERT_ID() )方法，使用主库
                        if (ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                            dynamicDataSourceGlobal = DynamicDataSourceGlobal.WRITE;
                        } else {
                            BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
                            String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");
                            if (sql.matches(REGEX)) {
                                dynamicDataSourceGlobal = DynamicDataSourceGlobal.WRITE;
                            } else {
                                dynamicDataSourceGlobal = DynamicDataSourceGlobal.READ;
                            }
                        }
                    } else {
                        dynamicDataSourceGlobal = DynamicDataSourceGlobal.WRITE;
                    }
                }
                log.warn("设置方法[{}] use [{}] Strategy, SqlCommandType [{}]..", ms.getId(),
                        dynamicDataSourceGlobal.name(), ms.getSqlCommandType().name());
                CACHE_MAP.put(ms.getId(), dynamicDataSourceGlobal);
            }
            DynamicDataSourceHolder.putDataSource(dynamicDataSourceGlobal);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String[] getForceUseWriteDataSourceSql() {
        if (this.properties == null) {
            return null;
        }
        if (this.properties.containsKey(FORCE_USE_WRITE_DATA_SOURCE_SQL)) {
            return (String[]) this.properties.get(FORCE_USE_WRITE_DATA_SOURCE_SQL);
        }
        return null;
    }
}
