package com.alpha.coding.common.mybatis;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.alpha.coding.common.mybatis.callback.TableUpdateListener;
import com.alpha.coding.common.mybatis.common.TableNameParser;
import com.alpha.coding.common.mybatis.common.TableUpdateDto;

import lombok.extern.slf4j.Slf4j;

/**
 * RecordUpdateInterceptor
 * <p>监听 insert/update 且主键为数值型的</p>
 *
 * <li>properties:配置，如includeTables=your_table_name</li>
 * <li>listener:监听器</li>
 * <li>keyColumnMap:主键映射，如your_table_name=id，默认为id</li>
 * <li>keyPropertyMap:主键对应属性映射，如your_table_name=id，默认为id</li>
 *
 * @version 1.0
 * Date: 2021/4/13
 */
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class})})
public class RecordUpdateInterceptor extends ShowSqlInterceptor {

    private static final Pattern PATTERN_KEY_COLUMN_DEFAULT = Pattern.compile("where\\s+id\\s?=\\s?\\d+");
    private static final Pattern PATTERN_KEY_VALUE = Pattern.compile("\\d+");
    private static final Map<String, Pattern> KEY_COLUMN_PATTERN_CACHE = new ConcurrentHashMap<>();

    private Properties properties;
    private TableUpdateListener listener;
    private Map<String, String> keyColumnMap;
    private Map<String, String> keyPropertyMap;

    public void setListener(TableUpdateListener listener) {
        this.listener = listener;
    }

    public void setKeyColumnMap(Map<String, String> keyColumnMap) {
        this.keyColumnMap = keyColumnMap;
    }

    public void setKeyPropertyMap(Map<String, String> keyPropertyMap) {
        this.keyPropertyMap = keyPropertyMap;
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
        Object result = invocation.proceed();
        try {
            if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())) {
                listener.onUpdate(new TableUpdateDto()
                        .setSqlId(sqlId)
                        .setTableName(tableName)
                        .setType(0)
                        .setId(parseIdFromParameterObject(tableName, boundSql)));
            } else if (SqlCommandType.UPDATE.equals(mappedStatement.getSqlCommandType())) {
                final TableUpdateDto dto = new TableUpdateDto()
                        .setSqlId(sqlId)
                        .setTableName(tableName)
                        .setType(1)
                        .setId(parseId(tableName, showSql(configuration, boundSql).toLowerCase()));
                listener.onUpdate(dto);
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

    private Long parseId(String tableName, String sql) {
        final Matcher matcher = findKeyColumnPattern(tableName).matcher(sql);
        if (matcher.find()) {
            final String where = matcher.group(0);
            final Matcher matcher1 = PATTERN_KEY_VALUE.matcher(where.split("=")[1]);
            if (matcher1.find()) {
                return Long.valueOf(matcher1.group(0));
            }
        }
        return null;
    }

    private Pattern findKeyColumnPattern(String tableName) {
        if (keyColumnMap == null || keyColumnMap.get(tableName) == null) {
            return PATTERN_KEY_COLUMN_DEFAULT;
        }
        return KEY_COLUMN_PATTERN_CACHE.computeIfAbsent(tableName,
                k -> Pattern.compile("where\\s+" + keyColumnMap.get(tableName).trim() + "\\s?=\\s?\\d+"));
    }

    private Long parseIdFromParameterObject(String tableName, BoundSql boundSql) {
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
            Class<?> targetClass = parameterObject.getClass();
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
        } catch (Exception e) {
            log.warn("parseId fail when insert for tableName={},keyProperty={},msg={}",
                    tableName, keyProperty, e.getMessage());
        }
        return null;
    }

}
