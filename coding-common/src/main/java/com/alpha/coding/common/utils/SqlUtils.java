package com.alpha.coding.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import com.alpha.coding.bo.base.Tuple;

/**
 * SqlUtils
 *
 * @author nick
 * @version 1.0
 * Date: 2020-02-21
 */
public class SqlUtils {

    public static String append(String s) {
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("'", "\\\\'");
        return "'" + ret + "'";
    }

    public static String append(Date d) {
        String dateStr = DateUtils.format(d, "yyyy-MM-dd");
        return "'" + dateStr + "'";
    }

    /**
     * change ' to \'
     *
     * @param s 输入
     * @return result
     */
    public static String escapeSql(String s) {
        if (s == null) {
            return null;
        }
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("'", "\\\\'");
        return ret;
    }

    /**
     * change % to \% change ' to \'
     *
     * @param s 输入
     * @return result
     */
    public static String escapeSqlLike(String s) {
        if (s == null) {
            return null;
        }
        String ret = s.replaceAll("\\\\", "\\\\\\\\");
        ret = ret.replaceAll("%", "\\\\%");
        ret = ret.replaceAll("'", "\\\\'");
        return ret;
    }

    public static String sqlLike(String str) {
        if (str == null) {
            return null;
        }
        return "%" + str + "%";
    }

    /**
     * 条件拼成sql
     *
     * @param condition 条件
     * @return result
     */
    public static String toConditionSql(List<String> condition) {
        if (condition == null || condition.size() <= 0) {
            return " ";
        }
        StringBuffer sb = new StringBuffer();
        sb.append(condition.get(0));
        for (int i = 1; i < condition.size(); i++) {
            sb.append(" and " + condition.get(i));
        }
        return sb.toString();
    }

    public static String toMd5List(Collection<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String url : urls) {
            sb.append("'").append(MD5Utils.md5(url)).append("',");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

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
            for (Field field : recordClass.getDeclaredFields()) {
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
            for (Field field : recordClass.getDeclaredFields()) {
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

    /**
     * 打印SQL，替换原始预编译SQL中的?为实际传入值
     */
    public static String printSQL(String sql, Object[] args) {
        if (args == null || args.length <= 0) {
            return sql;
        }
        final Object[] values = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String str = null;
            if (arg instanceof String) {
                str = "'" + arg.toString() + "'";
            } else if (arg instanceof Date) {
                str = "'" + DateUtils.format((Date) arg) + "'";
            } else if (arg instanceof byte[]) {
                str = "''";
            } else {
                if (arg != null) {
                    str = arg.toString();
                } else {
                    str = "NULL"; // 注意，null转化成NULL
                }
            }
            values[i] = str;
        }
        if (sql.contains("%")) {
            String[] tokens = sql.split("%", -1);
            int j = 0;
            for (int k = 0; k < tokens.length; k++) {
                while (tokens[k].contains("?")) {
                    tokens[k] = tokens[k].replaceFirst("\\?", "%s");
                    tokens[k] = String.format(tokens[k], values[j++]);
                }
            }
            return StringUtils.join(tokens, "%");
        } else {
            return String.format(sql.replaceAll("\\?", "%s"), values);
        }
    }

}
