package com.alpha.coding.common.mybatis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
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

import com.alpha.coding.common.utils.StringUtils;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})
})
public class DB2SqlInterceptor implements Interceptor {

    private static final String[] ARR1 = new String[] {
            "(.|\\n|\\r)*'(.|\\n|\\r)*\\s+limit\\s+(.|\\n|\\r)*'(.|\\n|\\r)*",
            "(.|\\n|\\r)*`limit`(.|\\n|\\r)*"};
    private static final String[] ARR2 = new String[] {
            "(.|\\n|\\r)*\\s+limit\\s+(\\d+|\\?),\\s{0,}(\\d+|\\?)(.|\\n|\\r)*",
            "(.|\\n|\\r)*\\s+limit\\s+(\\d+|\\?)\\s+offset\\s+(\\d+|\\?)(.|\\n|\\r)*",
            "(.|\\n|\\r)*\\s+limit\\s+(\\d+|\\?)(.|\\n|\\r)*"};
    private static final Map<String, Pattern> PATTERN_MAP = new HashMap<>();
    private static final String DB2_PAGE_TPL_FETCH_ONLY = "select row_number() over(%s) as rn__%d_, "
            + "p_t__%d_.* from(%s) p_t__%d_ fetch first %s rows only";
    private static final String DB2_PAGE_TPL_RANGE = "select * from(select row_number() over(%s) as rn__%d_, "
            + "p_t__%d_.* from(%s) p_t__%d_) where rn__%d_ between %s and %s";

    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = null;
        if (args.length > 1) {
            parameter = args[1];
        }

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        final String execSql = boundSql.getSql();
        if (StringUtils.isBlank(execSql) || !execSql.toLowerCase().contains(" limit ")) {
            return invocation.proceed();
        }
        // 计算参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Map<Integer, ParameterWrapper> parameterPositionMap = new HashMap<>();
        if (parameterMappings.size() > 0 && parameterObject != null) {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            int i = 0;
            final char[] chars = execSql.toCharArray();
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == '?') {
                    final ParameterMapping parameterMapping = parameterMappings.get(i++);
                    String propertyName = parameterMapping.getProperty();
                    try {
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            parameterPositionMap.put(j, new ParameterWrapper().setName(propertyName)
                                    .setMappingIndex(i - 1).setTarget(obj).setFromAdditional(false));
                            continue;
                        }
                    } catch (Exception e) {
                        log.debug("get property error for {}", propertyName, e);
                    }
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        parameterPositionMap.put(j, new ParameterWrapper().setName(propertyName)
                                .setMappingIndex(i - 1).setTarget(obj).setFromAdditional(true));
                    }
                }
            }
        }
        // 转换分页SQL
        final HashMap<String, String> params = new HashMap<>();
        String newSQL = convertLimitSql(execSql, new AtomicInteger(0),
                params, invocation, parameterPositionMap, 0);
        for (String key : params.keySet()) {
            newSQL = newSQL.replace(key, params.get(key));
        }
        log.info("convert to DB2-SQL => {}", newSQL);
        if (execSql.equals(newSQL)) {
            return invocation.proceed();
        }
        // 回写SQL
        MappedStatement newStatement = newMappedStatement(mappedStatement, new BoundSqlSqlSource(boundSql));
        MetaObject metaObject = SystemMetaObject.forObject(newStatement);
        metaObject.setValue("sqlSource.boundSql.sql", newSQL);
        args[0] = newStatement;
        return invocation.proceed();
    }

    @Data
    @Accessors(chain = true)
    private static class ParameterWrapper {
        private Object target;
        private String name;
        private boolean fromAdditional;
        private int mappingIndex;
    }

    /**
     * 递归处理
     */
    private static String convertLimitSql(String originSql, AtomicInteger paramsNo, Map<String, String> params,
                                          Invocation invocation, Map<Integer, ParameterWrapper> parameterPositionMap,
                                          int offsetOverOrigin) {
        paramsNo.incrementAndGet();
        if (originSql.contains("(")) {
            return handleLimit(originSql.substring(0, originSql.indexOf("(")) + "(" + convertLimitSql(
                    originSql.substring(originSql.indexOf("(") + 1, originSql.lastIndexOf(")")), paramsNo, params,
                    invocation, parameterPositionMap, offsetOverOrigin + originSql.indexOf("(") + 1) + ")"
                            + originSql.substring(originSql.lastIndexOf(")") + 1),
                    paramsNo, params, invocation, parameterPositionMap, offsetOverOrigin);
        } else {
            if (StringUtils.isBlank(originSql)) {
                return originSql;
            }
            return handleLimit(originSql, paramsNo, params, invocation, parameterPositionMap, offsetOverOrigin);
        }
    }

    /**
     * 处理limit
     */
    private static String handleLimit(String originSql, AtomicInteger paramsNo, Map<String, String> params,
                                      Invocation invocation, Map<Integer, ParameterWrapper> parameterPositionMap,
                                      int offsetOverOrigin) {
        final String lowerCaseSQL = originSql.toLowerCase();
        Function<Integer, String> function = t -> {
            StringBuilder offset = new StringBuilder();
            StringBuilder limit = new StringBuilder();
            int offsetOriginIndex = -1;
            int limitOriginIndex = -1;
            int start = lowerCaseSQL.lastIndexOf(" limit ");
            // int end = 0;
            int orderBySt = start;
            // int orderByEt = -1;
            String orderByParamKey = "$$$_order_by_" + paramsNo.incrementAndGet();
            final String orderByOpt = lowerCaseSQL.substring(0, start - 1);
            if (orderByOpt.lastIndexOf(" order by ") >= 0) {
                final String orderByTerm = originSql.substring(orderByOpt.lastIndexOf(" order by ") + 1, start);
                if (!orderByTerm.contains("'")) {
                    params.put(orderByParamKey, orderByTerm);
                    orderBySt = orderByOpt.lastIndexOf(" order by ") + 1;
                    // orderByEt = start;
                }
            }
            final char[] chars = originSql.toCharArray();
            if (t == 0) {
                boolean offsetFinish = false;
                boolean limitStart = false;
                for (int i = start + 7; i < chars.length; i++) {
                    if (chars[i] == ',' || chars[i] == ' ') {
                        if (limitStart) {
                            // end = i;
                            break;
                        }
                        offsetFinish = true;
                    } else if ((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '?') {
                        if (!offsetFinish) {
                            offset.append(chars[i]);
                            offsetOriginIndex = offsetOverOrigin + i;
                        } else {
                            limitStart = true;
                            limit.append(chars[i]);
                            limitOriginIndex = offsetOverOrigin + i;
                        }
                    }
                }
            } else if (t == 1) {
                boolean limitFinish = false;
                boolean offsetStart = false;
                for (int i = start + 7; i < chars.length; i++) {
                    if (chars[i] == ',' || chars[i] == ' ') {
                        if (offsetStart) {
                            // end = i;
                            break;
                        }
                        limitFinish = true;
                    } else if ((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '?') {
                        if (!limitFinish) {
                            limit.append(chars[i]);
                            limitOriginIndex = offsetOverOrigin + i;
                        } else {
                            offset.append(chars[i]);
                            offsetOriginIndex = offsetOverOrigin + i;
                        }
                    }
                }
            } else {
                boolean limitStart = false;
                for (int i = start + 7; i < chars.length; i++) {
                    if (chars[i] == ',' || chars[i] == ' ') {
                        if (limitStart) {
                            // end = i;
                            break;
                        }
                    } else if ((chars[i] >= '0' && chars[i] <= '9') || chars[i] == '?') {
                        limitStart = true;
                        limit.append(chars[i]);
                        limitOriginIndex = offsetOverOrigin + i;
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("parse SQL:[{}], get limit:[{}], offset:[{}], orderBy:[{}]", lowerCaseSQL,
                        limit.toString(), offset.toString(), params.get(orderByParamKey));
            }
            // 将limit转化为db2的分页查询
            String sql = originSql.substring(0, Math.min(orderBySt, start));
            if (t == 2) {
                return String.format(DB2_PAGE_TPL_FETCH_ONLY, orderBySt < start ? orderByParamKey : "",
                        paramsNo.get(), paramsNo.get(), sql, paramsNo.get(), limit.toString());
            } else {
                int offsetVal = 0;
                if (offset.indexOf("?") >= 0) {
                    final ParameterWrapper wrapper = parameterPositionMap.get(offsetOriginIndex);
                    offsetVal = Integer.parseInt(String.valueOf(wrapper.getTarget()));
                } else {
                    offsetVal = Integer.parseInt(offset.toString());
                }
                int limitVal = 0;
                if (limit.indexOf("?") >= 0) {
                    final ParameterWrapper wrapper = parameterPositionMap.get(limitOriginIndex);
                    limitVal = Integer.parseInt(String.valueOf(wrapper.getTarget()));
                } else {
                    limitVal = Integer.parseInt(limit.toString());
                }
                int betweenVal1 = offsetVal + 1;
                int betweenVal2 = offsetVal + limitVal;
                if (t == 1) {
                    // 回写参数 limit => betweenVal1
                    if (limit.indexOf("?") >= 0) {
                        writeNewValue(invocation, parameterPositionMap.get(limitOriginIndex), betweenVal1);
                    }
                    // 回写参数 offset => betweenVal2
                    if (offset.indexOf("?") >= 0) {
                        writeNewValue(invocation, parameterPositionMap.get(offsetOriginIndex), betweenVal2);
                    }
                    return String.format(DB2_PAGE_TPL_RANGE, orderBySt < start ? orderByParamKey : "",
                            paramsNo.get(), paramsNo.get(), sql, paramsNo.get(), paramsNo.get(),
                            limit.indexOf("?") >= 0 ? limit.toString() : betweenVal1,
                            offset.indexOf("?") >= 0 ? offset.toString() : betweenVal2);
                } else {
                    // 回写参数 offset => betweenVal1
                    if (offset.indexOf("?") >= 0) {
                        writeNewValue(invocation, parameterPositionMap.get(offsetOriginIndex), betweenVal1);
                    }
                    // 回写参数 limit => betweenVal2
                    if (limit.indexOf("?") >= 0) {
                        writeNewValue(invocation, parameterPositionMap.get(limitOriginIndex), betweenVal2);
                    }
                    return String.format(DB2_PAGE_TPL_RANGE, orderBySt < start ? orderByParamKey : "",
                            paramsNo.get(), paramsNo.get(), sql, paramsNo.get(), paramsNo.get(),
                            offset.indexOf("?") >= 0 ? offset.toString() : betweenVal1,
                            limit.indexOf("?") >= 0 ? limit.toString() : betweenVal2);
                }
            }
        };
        for (String s : ARR1) {
            for (int j = 0; j < ARR2.length; j++) {
                final Pattern pattern = PATTERN_MAP.computeIfAbsent(s + ARR2[j], Pattern::compile);
                if (pattern.matcher(lowerCaseSQL).matches()) {
                    return function.apply(j);
                }
            }
            final Pattern pattern = PATTERN_MAP.computeIfAbsent(s, Pattern::compile);
            if (pattern.matcher(lowerCaseSQL).matches()) {
                return originSql;
            }
        }
        for (int j = 0; j < ARR2.length; j++) {
            final Pattern pattern = PATTERN_MAP.computeIfAbsent(ARR2[j], Pattern::compile);
            if (pattern.matcher(lowerCaseSQL).matches()) {
                return function.apply(j);
            }
        }
        return originSql;
    }

    private static void writeNewValue(Invocation invocation, ParameterWrapper wrapper, Object newVal) {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        Object parameterObject = boundSql.getParameterObject();
        if (!wrapper.isFromAdditional()) {
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            try {
                metaObject.setValue(wrapper.getName(), newVal);
            } catch (UnsupportedOperationException e) {
                // 针对DynamicSQL的处理,parameters使用了Collections.unmodifiableMap无法更新,先替换
                if ("org.mybatis.dynamic.sql.select.render.DefaultSelectStatementProvider"
                        .equals(parameterObject.getClass().getName())) {
                    Map<String, Object> origin = (Map<String, Object>) metaObject.getValue("parameters");
                    final Map<Object, Object> map = new LinkedHashMap<>();
                    origin.forEach(map::put);
                    metaObject.setValue("parameters", map);
                    metaObject.setValue(wrapper.getName(), newVal);
                    return;
                }
                throw e;
            }
        } else {
            boundSql.setAdditionalParameter(wrapper.getName(), newVal);
        }
    }

    @Override
    public Object plugin(Object obj) {
        return Plugin.wrap(obj, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static MappedStatement newMappedStatement(MappedStatement ms, SqlSource sqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(),
                sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    public static class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

}

