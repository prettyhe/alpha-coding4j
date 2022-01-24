package com.alpha.coding.common.mybatis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ClassUtils;

import com.alpha.coding.common.mybatis.callback.RecordUpdateStub;
import com.alpha.coding.common.mybatis.callback.TableUpdateListener;
import com.alpha.coding.common.mybatis.common.TableNameParser;
import com.alpha.coding.common.mybatis.common.TableUpdateBeforeControl;
import com.alpha.coding.common.mybatis.common.TableUpdateDto;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * RecordUpdateInterceptor
 * <p>监听 insert/update 且主键为数值型的</p>
 *
 * <li>properties:配置，如includeTables=your_table_name</li>
 * <li>listener:监听器</li>
 * <li>keyColumnMap:主键映射，如your_table_name=id，默认为id</li>
 * <li>keyPropertyMap:主键对应属性映射，如your_table_name=id，默认为id</li>
 * <li>recordUpdateStubMap:表更新存根，如your_table_name=your_RecordUpdateStub</li>
 *
 * @version 1.0
 * Date: 2021/4/13
 */
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class})})
public class RecordUpdateInterceptor extends ShowSqlInterceptor {

    private static final Pattern PATTERN_KEY_COLUMN_DEFAULT = Pattern.compile("where\\s+id\\s*=\\s*\\d+");
    private static final Pattern PATTERN_KEY_COLUMN_IN_DEFAULT =
            Pattern.compile("where\\s+id\\s+in\\s*\\(\\s*\\d+\\s*(,\\s*\\d+\\s*)*\\)");
    private static final Pattern PATTERN_KEY_VALUE = Pattern.compile("\\d+");
    private static final Map<String, Pattern> KEY_COLUMN_PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Pattern> KEY_COLUMN_IN_PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final String DYNAMIC_INSERT_CLASS_NAME =
            "org.mybatis.dynamic.sql.insert.render.InsertStatementProvider";

    private Properties properties;
    private TableUpdateListener listener;
    private Map<String, String> keyColumnMap;
    private Map<String, String> keyPropertyMap;
    private Map<String, RecordUpdateStub> recordUpdateStubMap;

    public void setListener(TableUpdateListener listener) {
        this.listener = listener;
    }

    public void setKeyColumnMap(Map<String, String> keyColumnMap) {
        this.keyColumnMap = keyColumnMap;
    }

    public void setKeyPropertyMap(Map<String, String> keyPropertyMap) {
        this.keyPropertyMap = keyPropertyMap;
    }

    public void setRecordUpdateStubMap(Map<String, RecordUpdateStub> recordUpdateStubMap) {
        this.recordUpdateStubMap = recordUpdateStubMap;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Set<String> includeTables = parseIncludeTables();
        if (listener == null || includeTables == null || includeTables.isEmpty()) {
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        final String sqlId = mappedStatement.getId();
        final BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        final Configuration configuration = mappedStatement.getConfiguration();
        final String originalSql = boundSql.getSql().replaceAll("[\\s]+", " ");
        Collection<String> tables = null;
        try {
            tables = new TableNameParser(originalSql.toLowerCase()).tables();
        } catch (Exception e) {
            log.warn("parse table fail from {} in {}", originalSql, sqlId);
            return invocation.proceed();
        }
        if (tables == null || tables.isEmpty()) {
            return invocation.proceed();
        }
        final String tableName = tables.iterator().next();
        if (!includeTables.contains(tableName)) {
            return invocation.proceed();
        }
        final Map<Long, TableUpdateDto> updateDtoMap = new LinkedHashMap<>();
        if (SqlCommandType.UPDATE.equals(mappedStatement.getSqlCommandType())
                && (recordUpdateStubMap != null && recordUpdateStubMap.containsKey(tableName))) {
            try {
                buildUpdateDtoMap(configuration, boundSql, tableName, updateDtoMap);
            } catch (Exception e) {
                log.warn("build update record fail, sqlId={}, table={}", sqlId, tableName, e);
            }
        }
        Object result = invocation.proceed();
        try {
            final long timestamp = System.currentTimeMillis();
            if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())) {
                final TableUpdateDto dto = new TableUpdateDto()
                        .setTimestamp(timestamp)
                        .setSqlId(sqlId)
                        .setTableName(tableName)
                        .setType(0)
                        .setId(parseKeyFromParameterObject(tableName, boundSql));
                dto.setAfter(queryByPrimaryKey(tableName, dto.getId()));
                dto.appendBizParam(TableUpdateBeforeControl.getCopyOfContextMap());
                TableUpdateBeforeControl.clear();
                listener.onUpdate(dto);
            } else if (SqlCommandType.UPDATE.equals(mappedStatement.getSqlCommandType())) {
                if (recordUpdateStubMap != null && recordUpdateStubMap.containsKey(tableName)) {
                    updateDtoMap.forEach((k, v) -> v.setAfter(queryByPrimaryKey(tableName, k)));
                } else {
                    buildUpdateDtoMap(configuration, boundSql, tableName, updateDtoMap);
                }
                final Map<String, Object> beforeControlContext = TableUpdateBeforeControl.getCopyOfContextMap();
                TableUpdateBeforeControl.clear();
                updateDtoMap.forEach((k, v) -> {
                    v.setTimestamp(timestamp);
                    v.setSqlId(sqlId);
                    v.appendBizParam(beforeControlContext);
                    listener.onUpdate(v);
                });
            }
        } catch (Exception e) {
            log.warn("parse TableUpdateDto fail from {} in {}", originalSql, sqlId, e);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private Set<String> parseIncludeTables() {
        final String prop = getProperty("includeTables");
        return prop == null ? Collections.emptySet() : Arrays.stream(prop.split(",")).map(String::trim)
                .filter(x -> !x.isEmpty()).collect(Collectors.toSet());
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

    private Long parseKey(String tableName, String sql) {
        final Matcher matcher = findKeyColumnPattern(tableName).matcher(sql);
        if (matcher.find()) {
            final String where = matcher.group(0);
            final Matcher keyMatcher = PATTERN_KEY_VALUE.matcher(where.split("=")[1]);
            if (keyMatcher.find()) {
                return Long.valueOf(keyMatcher.group(0));
            }
        }
        return null;
    }

    private Set<Long> parseKeys(String tableName, String sql) {
        final Matcher matcher = findKeyColumnInPattern(tableName).matcher(sql);
        if (matcher.find()) {
            final String where = matcher.group(0);
            final String ids = where.split("\\(")[1].split("\\)")[0];
            return Arrays.stream(ids.split(",")).map(String::trim).filter(StringUtils::isNotBlank)
                    .map(Long::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return null;
    }

    private Pattern findKeyColumnPattern(String tableName) {
        if (keyColumnMap == null || keyColumnMap.get(tableName) == null) {
            return PATTERN_KEY_COLUMN_DEFAULT;
        }
        return KEY_COLUMN_PATTERN_CACHE.computeIfAbsent(tableName,
                k -> Pattern.compile("where\\s+" + keyColumnMap.get(tableName).trim() + "\\s*=\\s*\\d+"));
    }

    private Pattern findKeyColumnInPattern(String tableName) {
        if (keyColumnMap == null || keyColumnMap.get(tableName) == null) {
            return PATTERN_KEY_COLUMN_IN_DEFAULT;
        }
        return KEY_COLUMN_IN_PATTERN_CACHE.computeIfAbsent(tableName,
                k -> Pattern.compile("where\\s+" + keyColumnMap.get(tableName).trim()
                        + "\\s+in\\s*\\(\\s*\\d+\\s*(,\\s*\\d+\\s*)*\\)"));
    }

    private Long parseKeyFromParameterObject(String tableName, BoundSql boundSql) {
        if (boundSql == null) {
            return null;
        }
        final Object parameterObject = boundSql.getParameterObject();
        if (parameterObject == null) {
            return null;
        }
        String keyProperty = "id";
        try {
            if (keyPropertyMap != null && keyPropertyMap.get(tableName) != null) {
                keyProperty = keyPropertyMap.get(tableName);
            }
            if (parameterObject instanceof Map) {
                return Long.valueOf(String.valueOf(((Map) parameterObject).get(keyProperty)));
            }
            final Class<?> targetClass = parameterObject.getClass();
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (ClassUtils.isPresent(DYNAMIC_INSERT_CLASS_NAME, contextClassLoader)) {
                Class<?> insertStatementProvider = ClassUtils.forName(DYNAMIC_INSERT_CLASS_NAME, contextClassLoader);
                if (insertStatementProvider.isAssignableFrom(targetClass)) {
                    final Method getRecordMethod = insertStatementProvider.getMethod("getRecord");
                    final Object record = getRecordMethod.invoke(parameterObject);
                    return doParseKeyFromField(record.getClass(), record, keyProperty);
                }
            }
            return doParseKeyFromField(targetClass, parameterObject, keyProperty);
        } catch (Exception e) {
            log.warn("parseId fail when insert for tableName={},keyProperty={},msg={}",
                    tableName, keyProperty, e.getMessage());
        }
        return null;
    }

    private Long doParseKeyFromField(Class<?> targetClass, Object parameterObject, String keyProperty)
            throws IllegalAccessException {
        while (targetClass != Object.class) {
            for (Field field : targetClass.getDeclaredFields()) {
                if (keyProperty.equals(field.getName())) {
                    field.setAccessible(true);
                    final Object val = field.get(parameterObject);
                    return val == null ? null : Long.valueOf(String.valueOf(val));
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        return null;
    }

    private Object queryByPrimaryKey(String tableName, Long key) {
        if (key == null) {
            return null;
        }
        if (recordUpdateStubMap != null && recordUpdateStubMap.containsKey(tableName)) {
            try {
                return recordUpdateStubMap.get(tableName).selectByPrimaryKey(key);
            } catch (Exception e) {
                log.warn("queryByPrimaryKey fail for table={},PrimaryKey={}", tableName, key, e);
            }
        }
        return null;
    }

    private void buildUpdateDtoMap(Configuration configuration, BoundSql boundSql, String tableName,
                                   Map<Long, TableUpdateDto> dtoMap) {
        final String sql = showSql(configuration, boundSql).toLowerCase();
        final Long id = parseKey(tableName, sql);
        if (id != null) {
            final TableUpdateDto dto = new TableUpdateDto()
                    .setTableName(tableName)
                    .setType(1)
                    .setId(id)
                    .setBefore(queryByPrimaryKey(tableName, id));
            dtoMap.put(id, dto);
        } else {
            final Set<Long> set = parseKeys(tableName, sql);
            if (set != null && !set.isEmpty()) {
                set.forEach(p -> {
                    final TableUpdateDto dto = new TableUpdateDto()
                            .setTableName(tableName)
                            .setType(1)
                            .setId(p)
                            .setBefore(queryByPrimaryKey(tableName, p))
                            .setIds(set);
                    dtoMap.put(p, dto);
                });
            }
        }
    }

}
