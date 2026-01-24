package com.alpha.coding.common.utils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.common.mybatis.common.DbType;

import lombok.extern.slf4j.Slf4j;

/**
 * SqlUtils
 *
 * @author nick
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class SqlUtils {

    private static final int STATE_NORMAL = 0;
    private static final int STATE_SINGLE_QUOTE = 1; // 单引号
    private static final int STATE_DOUBLE_QUOTE = 2; // 双引号
    private static final int STATE_SINGLE_LINE_COMMENT = 3; // 单行注释
    private static final int STATE_MULTI_LINE_COMMENT = 4; // 多行注释
    private static final int STATE_BACKTICK = 5;    // MySQL反引号
    private static final int STATE_SQUARE_BRACKET = 6; // SQL Server方括号
    private static final int STATE_ANSI_IDENTIFIER = 7; // ANSI双引号标识符

    private static final Map<String, List<Integer>> TEMPLATE_CACHE = new ConcurrentHashMap<>(256);

    // SQL关键字模式（用于ANSI标识符判断）
    private static final Pattern ANSI_KEYWORD_PATTERN = Pattern.compile(
            "(?i)\\b(FROM|JOIN|INTO|USE|TABLE|COLUMN|SELECT|WHERE|SET|HAVING|GROUP\\s+BY|ORDER\\s+BY)\\b\\s*$");

    private static final List<Class<?>> PRINT_RESULT_VALUE_TYPES = Arrays.asList(boolean.class,
            byte.class, char.class, double.class, float.class, int.class, long.class, short.class,
            Boolean.class, Byte.class, Character.class, Double.class, Float.class, Long.class, Short.class);

    /**
     * 生成InsertSelective语句
     *
     * @param entity 实体对象
     */
    public static Tuple<String, List> genInsertSelective(Object entity) {
        final Class<?> recordClass = entity.getClass();
        if (!recordClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("对象未标记为@Table");
        }
        try {
            List args = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("insert into ").append(recordClass.getDeclaredAnnotation(Table.class).name()).append(" (");
            for (Field field : FieldUtils.getDeclaredFieldsWithCache(recordClass)) {
                if (!field.isAnnotationPresent(Column.class)) {
                    continue;
                }
                field.setAccessible(true);
                final Column column = field.getDeclaredAnnotation(Column.class);
                final Object val = field.get(entity);
                if (val != null) {
                    sb.append(column.name()).append(",");
                    args.add(val);
                }
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(") values (");
            args.forEach(p -> sb.append("?,"));
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append(")");
            return Tuple.of(sb.toString(), args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成UpdateByPrimaryKeySelective语句
     *
     * @param entity 实体对象
     */
    public static Tuple<String, List> genUpdateByPrimaryKeySelective(Object entity) {
        final Class<?> recordClass = entity.getClass();
        if (!recordClass.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("对象未标记为@Table");
        }
        try {
            List args = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            Field primaryKeyField = null;
            sb.append("update ").append(recordClass.getDeclaredAnnotation(Table.class).name()).append(" set ");
            for (Field field : FieldUtils.getDeclaredFieldsWithCache(recordClass)) {
                if (!field.isAnnotationPresent(Column.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(Id.class)) {
                    primaryKeyField = field;
                    continue;
                }
                field.setAccessible(true);
                final Column column = field.getDeclaredAnnotation(Column.class);
                final Object val = field.get(entity);
                if (val != null) {
                    sb.append(column.name()).append("=?,");
                    args.add(val);
                }
            }
            if (primaryKeyField == null) {
                throw new RuntimeException("找不到表" + recordClass.getDeclaredAnnotation(Table.class).name() + "主键");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            primaryKeyField.setAccessible(true);
            sb.append(" where ").append(primaryKeyField.getDeclaredAnnotation(Column.class).name()).append("=?");
            args.add(primaryKeyField.get(entity));
            return Tuple.of(sb.toString(), args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String escapeSQLString(String sql) {
        return sql.replace("'", "''");
    }

    /**
     * 将值转成字符串，也即进行SQL转义，针对byte[]类型的转为byte[length]
     */
    public static String formatValueToSQLString(Object target) {
        String dateStr;
        if (target == null) {
            return "NULL";
        } else if (target instanceof String) {
            return "'" + escapeSQLString((String) target) + "'";
        } else if (target instanceof byte[]) {
            return "'byte[" + ((byte[]) target).length + "]'";
        } else if ((dateStr = tryFormatDateForSQL(target)) != null) {
            return "'" + escapeSQLString(dateStr) + "'";
        } else {
            return escapeSQLString(target.toString());
        }
    }

    /**
     * 检查是否是时间日期类类型并格式化
     */
    public static String tryFormatDateForSQL(Object target) {
        if (target instanceof java.sql.Date) {
            return DateUtils.format((Date) target, DateUtils.DATE_FORMAT);
        } else if (target instanceof java.sql.Time) {
            return DateUtils.format((Date) target, DateUtils.TIME_FORMAT);
        } else if (target instanceof Date) {
            return DateUtils.format((Date) target, DateUtils.DEFAULT_FORMAT);
        } else if (target instanceof LocalDate) {
            return ((LocalDate) target).format(DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT));
        } else if (target instanceof LocalTime) {
            return ((LocalTime) target).format(DateTimeFormatter.ofPattern(DateUtils.TIME_FORMAT));
        } else if (target instanceof LocalDateTime) {
            return ((LocalDateTime) target).format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_FORMAT));
        }
        return null;
    }

    /**
     * 将SQL执行结果格式化，仅针对返回单个结果的，如insert/update/delete类操作的
     */
    public static String formatSQLExecResult(Object returnValue) {
        Object target = returnValue;
        if (returnValue instanceof Collection && ((Collection<?>) returnValue).size() == 1) {
            target = ((Collection<?>) returnValue).iterator().next();
        }
        if (target instanceof Map && ((Map<?, ?>) target).size() == 1) {
            target = ((Map<?, ?>) target).values().iterator().next();
        }
        if (target == null) {
            return null;
        }
        if (target instanceof Number || PRINT_RESULT_VALUE_TYPES.contains(target.getClass())) {
            return target.toString();
        }
        final String formatDateForSQL = tryFormatDateForSQL(target);
        if (formatDateForSQL == null) {
            if (returnValue instanceof Collection) {
                return "list.size=" + ((Collection<?>) returnValue).size();
            } else if (returnValue instanceof Map) {
                return "map.size=" + ((Map<?, ?>) returnValue).size();
            }
        }
        return formatDateForSQL;
    }

    /**
     * 打印SQL，替换原始预编译SQL中的?为实际传入值
     */
    public static String printSQL(String sql, Object[] args) {
        if (args == null || args.length == 0) {
            return sql;
        }
        final StringBuilder out = new StringBuilder(sql.length() + 128);
        int idx = 0;
        int len = sql.length();
        for (int i = 0; i < len; i++) {
            char c = sql.charAt(i);
            if (c == '?' && idx < args.length) {
                out.append(formatValueToSQLString(args[idx++]));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * 主入口：替换SQL中的占位符
     *
     * @param sql    原始SQL（含?占位符）
     * @param params 参数列表
     * @param dbType 数据库类型（可为空，会尝试自动检测）
     * @return 完整SQL（仅用于日志/调试，不可直接执行！）
     */
    public static String replacePlaceholders(String sql, Object[] params, DbType dbType) {
        if (sql == null || !sql.contains("?")) {
            return sql;
        }
        try {
            // 1. 获取占位符位置
            List<Integer> placeholderPositions = findPlaceholderPositions(sql, dbType);
            // 2. 验证参数数量
            validateParameters(placeholderPositions.size(), params.length, sql);
            // 3. 执行替换（从后往前避免索引偏移）
            return performReplacement(sql, params, placeholderPositions);
        } catch (Exception e) {
            log.error("replacePlaceholders for sql fail: {}, error is {}.{}", sql,
                    e.getClass().getName(), e.getMessage());
            // 降级策略：保守替换（可能不准确但保证不崩溃）
            return fallbackReplacement(sql, params);
        }
    }

    /**
     * 验证参数数量
     */
    private static void validateParameters(int placeholderCount, int paramCount, String sql) {
        if (placeholderCount != paramCount) {
            throw new IllegalArgumentException(
                    String.format("参数数量不匹配: SQL需要 %d 个参数，但提供了 %d 个。SQL: %s",
                            placeholderCount, paramCount, sql));
        }
    }

    /**
     * 查找所有真正的占位符位置
     */
    private static List<Integer> findPlaceholderPositions(String sql, DbType dbType) {
        sql = sql.toLowerCase();
        // 尝试从缓存获取
        String cacheKey = MD5Utils.md5(Optional.ofNullable(dbType).map(DbType::name).orElse("") + ":" + sql);
        if (TEMPLATE_CACHE.containsKey(cacheKey)) {
            return new ArrayList<>(TEMPLATE_CACHE.get(cacheKey));
        }

        final List<Integer> positions = new ArrayList<>();
        int state = STATE_NORMAL;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            switch (state) {
                case STATE_NORMAL:
                    state = processNormalState(sql, dbType, i, c, positions);
                    break;
                case STATE_SINGLE_QUOTE:
                    state = processSingleQuoteState(sql, i, c);
                    break;
                case STATE_DOUBLE_QUOTE:
                    state = processDoubleQuoteState(sql, dbType, i, c);
                    break;
                case STATE_BACKTICK:
                    state = processBacktickState(sql, i, c);
                    break;
                case STATE_SQUARE_BRACKET:
                    state = processSquareBracketState(sql, i, c);
                    break;
                case STATE_ANSI_IDENTIFIER:
                    state = processAnsiIdentifierState(sql, i, c);
                    break;
                case STATE_SINGLE_LINE_COMMENT:
                    state = processSingleLineCommentState(sql, i, c);
                    break;
                case STATE_MULTI_LINE_COMMENT:
                    state = processMultiLineCommentState(sql, i, c);
                    break;
            }
        }

        // 缓存结果（避免重复解析）
        TEMPLATE_CACHE.put(cacheKey, new ArrayList<>(positions));

        return positions;
    }

    //region 状态处理方法
    private static int processNormalState(String sql, DbType dbType, int i, char c, List<Integer> positions) {
        if (c == '\'') {
            return STATE_SINGLE_QUOTE;
        } else if (c == '"' && isDoubleQuoteForString(sql, dbType, i)) {
            return STATE_DOUBLE_QUOTE;
        } else if ((dbType == DbType.MYSQL || dbType == DbType.MARIADB) && c == '`') {
            return STATE_BACKTICK;
        } else if (dbType == DbType.SQL_SERVER && c == '[') {
            return STATE_SQUARE_BRACKET;
        } else if (c == '"' && !isDoubleQuoteForString(sql, dbType, i)) {
            return STATE_ANSI_IDENTIFIER;
        } else if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
            return STATE_SINGLE_LINE_COMMENT;
        } else if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
            return STATE_MULTI_LINE_COMMENT;
        } else if (c == '#' && (dbType == DbType.MYSQL || dbType == DbType.MARIADB)) {
            return STATE_SINGLE_LINE_COMMENT; // MySQL风格单行注释
        } else if (c == '?') {
            // 关键：通过上下文判断是否为真正的占位符
            if (isRealPlaceholder(sql, i)) {
                positions.add(i);
            }
        }
        return STATE_NORMAL;
    }

    private static int processSingleQuoteState(String sql, int i, char c) {
        if (c == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
            return STATE_NORMAL;
        }
        return STATE_SINGLE_QUOTE;
    }

    private static boolean isDoubleQuoteForString(String sql, DbType dbType, int pos) {
        // 在ANSI模式下，双引号用于标识符而非字符串
        if (dbType == DbType.MYSQL || dbType == DbType.MARIADB) {
            return true; // MySQL中双引号总是字符串
        }
        // 其他数据库：检查上下文
        if (pos == 0) {
            return true;
        }
        char prev = sql.charAt(pos - 1);
        // 常见的标识符前导字符
        boolean likelyIdentifier = Character.isWhitespace(prev) || ",.()=<>!+-*/".indexOf(prev) != -1;
        // 检查是否在关键字后（更可能是标识符）
        if (pos > 5) {
            String preceding = sql.substring(Math.max(0, pos - 10), pos).toUpperCase();
            if (ANSI_KEYWORD_PATTERN.matcher(preceding).find()) {
                return false; // 可能是标识符
            }
        }
        return !likelyIdentifier;
    }

    private static int processDoubleQuoteState(String sql, DbType dbType, int i, char c) {
        if (c == '"' && (i == 0 || sql.charAt(i - 1) != '\\')) {
            return STATE_NORMAL;
        }
        return STATE_DOUBLE_QUOTE;
    }

    private static int processBacktickState(String sql, int i, char c) {
        if (c == '`') {
            // MySQL: 双反引号表示转义
            if (i + 1 < sql.length() && sql.charAt(i + 1) == '`') {
                i++; // 跳过下一个反引号
            } else {
                return STATE_NORMAL;
            }
        }
        return STATE_BACKTICK;
    }

    private static int processSquareBracketState(String sql, int i, char c) {
        if (c == ']') {
            // SQL Server: 双]表示转义
            if (i + 1 < sql.length() && sql.charAt(i + 1) == ']') {
                i++; // 跳过下一个]
            } else {
                return STATE_NORMAL;
            }
        }
        return STATE_SQUARE_BRACKET;
    }

    private static int processAnsiIdentifierState(String sql, int i, char c) {
        if (c == '"') {
            // ANSI: 双"表示转义
            if (i + 1 < sql.length() && sql.charAt(i + 1) == '"') {
                i++; // 跳过下一个"
            } else {
                return STATE_NORMAL;
            }
        }
        return STATE_ANSI_IDENTIFIER;
    }

    private static int processSingleLineCommentState(String sql, int i, char c) {
        if (c == '\n' || c == '\r') {
            return STATE_NORMAL;
        }
        return STATE_SINGLE_LINE_COMMENT;
    }

    private static int processMultiLineCommentState(String sql, int i, char c) {
        if (c == '*' && i + 1 < sql.length() && sql.charAt(i + 1) == '/') {
            return STATE_NORMAL;
        }
        return STATE_MULTI_LINE_COMMENT;
    }

    /**
     * 核心判断：是否为真正的占位符（上下文感知）
     */
    private static boolean isRealPlaceholder(String sql, int pos) {
        // 规则1: 不能在引号/注释/标识符内（由状态机保证）

        // 规则2: 前面的字符必须是"安全"的
        boolean prevValid = (pos == 0) || isValidPrevChar(sql.charAt(pos - 1));
        // 规则3: 后面的字符必须是"安全"的
        boolean nextValid = (pos == sql.length() - 1) || isValidNextChar(sql.charAt(pos + 1));
        // 规则4: 特殊上下文检查（增强准确性）
        if (prevValid && nextValid) {
            return isSafePlaceholderContext(sql, pos);
        }
        return false;
    }

    private static boolean isValidPrevChar(char c) {
        return Character.isWhitespace(c)
                || "=<>(){}[],+-*/%".indexOf(c) != -1
                || c == '\n' || c == '\r' || c == '\t';
    }

    private static boolean isValidNextChar(char c) {
        return Character.isWhitespace(c)
                || "=<>(){}[],+-*/%;".indexOf(c) != -1
                || c == '\n' || c == '\r' || c == '\t' || isEndOfStatement(c);
    }

    private static boolean isEndOfStatement(char c) {
        return c == ';';
    }

    private static boolean isSafePlaceholderContext(String sql, int pos) {
        // 检查是否在函数调用中 (func(?, ?))
        if (pos > 4) {
            String preceding = sql.substring(Math.max(0, pos - 5), pos).toLowerCase();
            if (preceding.matches(".*,\\s*") || preceding.endsWith("(")) {
                return true;
            }
        }
        // 检查是否在VALUES子句中
        if (pos > 10) {
            String before = sql.substring(0, pos).toLowerCase();
            if (before.contains("values") && before.lastIndexOf("values") > before.lastIndexOf(")")) {
                return true;
            }
        }
        // 检查是否在INSERT列中
        if (pos > 15) {
            String before = sql.substring(0, pos).toLowerCase();
            if (before.contains("insert") && before.contains("(")
                    && before.lastIndexOf("(") > before.lastIndexOf("insert")) {
                return true;
            }
        }
        return true; // 默认认为是安全的
    }

    /**
     * 执行参数替换
     */
    private static String performReplacement(String sql, Object[] params, List<Integer> positions) {
        StringBuilder result = new StringBuilder(sql);
        // 从后往前替换，避免索引偏移
        for (int i = positions.size() - 1; i >= 0; i--) {
            int pos = positions.get(i);
            Object param = i < params.length ? params[i] : null;
            String replacement = formatValueToSQLString(param);
            result.replace(pos, pos + 1, replacement);
        }
        return result.toString();
    }

    /**
     * 降级替换策略（当精确替换失败时）
     */
    private static String fallbackReplacement(String sql, Object[] params) {
        StringBuilder result = new StringBuilder(sql);
        int paramCount = Math.min(countPlaceholders(sql), params.length);
        // 简单替换：从后往前替换前N个问号
        int pos = result.length();
        int replaced = 0;
        while (pos >= 0 && replaced < paramCount) {
            pos = result.lastIndexOf("?", pos - 1);
            if (pos < 0) {
                break;
            }
            // 简单检查：跳过引号内的问号（不精确但足够用于降级）
            if (!isInsideQuotes(result.toString(), pos)) {
                String replacement = formatValueToSQLString(params[replaced]);
                result.replace(pos, pos + 1, replacement);
                replaced++;
            }
        }
        return result.toString();
    }

    /**
     * 降级策略用：简单检查是否在引号内
     */
    private static boolean isInsideQuotes(String sql, int pos) {
        boolean inSingle = false;
        boolean inDouble = false;
        for (int i = 0; i < pos; i++) {
            char c = sql.charAt(i);
            if (c == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                inSingle = !inSingle;
            } else if (c == '"' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                inDouble = !inDouble;
            }
        }
        return inSingle || inDouble;
    }

    /**
     * 计算SQL中问号的总数（用于降级策略）
     */
    private static int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

}
