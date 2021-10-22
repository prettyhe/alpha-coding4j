package com.alpha.coding.common.message.dal;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.alpha.coding.bo.base.Tuple;
import com.alpha.coding.bo.function.common.Functions;
import com.alpha.coding.common.jdbc.MyBeanPropertyRowMapper;
import com.alpha.coding.common.message.DependencyHolder;
import com.alpha.coding.common.utils.ClassUtils;
import com.alpha.coding.common.utils.SqlUtils;
import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * MessageMonitorDao
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Slf4j
@Component
public class MessageMonitorDao {

    @Autowired
    private DependencyHolder dependencyHolder;

    private Object executeAndLogSQL(String sql, Object[] args, BiFunction<String, Object[], Object> execute) {
        String printSQL = sql;
        try {
            printSQL = SqlUtils.printSQL(sql, args);
        } catch (Exception e) {
            log.warn("拼装SQL异常 => {}", sql, e);
        }
        Throwable throwable = null;
        final long startNanoTime = System.nanoTime();
        try {
            return execute.apply(sql, args);
        } catch (Throwable e) {
            throwable = e;
            throw e;
        } finally {
            final long endNanoTime = System.nanoTime();
            if (throwable != null) {
                log.warn("execute-sql fail, {}: {}; msg: {}, cost {}",
                        StringUtils.abbreviateDotSplit(ClassUtils.getCallerCallerClassName(), 2),
                        printSQL, throwable.getMessage(), Functions.formatNanos.apply(endNanoTime - startNanoTime));
            } else {
                log.info("{}: {}; cost {}",
                        StringUtils.abbreviateDotSplit(ClassUtils.getCallerCallerClassName(), 2),
                        printSQL, Functions.formatNanos.apply(endNanoTime - startNanoTime));
            }
        }
    }

    public int insertSelective(MessageMonitor messageMonitor) {
        final Tuple<String, List> tuple = SqlUtils.genInsertSelective(messageMonitor);
        final Object[] args = tuple.getS().toArray(new Object[0]);
        return (int) executeAndLogSQL(tuple.getF(), args,
                (a, b) -> {
                    final KeyHolder keyHolder = new GeneratedKeyHolder();
                    final int result = dependencyHolder.jdbcTemplate().update(connection -> {
                        final PreparedStatement ps = connection.prepareStatement(a, Statement.RETURN_GENERATED_KEYS);
                        for (int i = 0; i < b.length; i++) {
                            ps.setObject(i + 1, b[i]);
                        }
                        return ps;
                    }, keyHolder);
                    if (result > 0) {
                        messageMonitor.setId(keyHolder.getKey().longValue());
                    }
                    return result;
                });
    }

    public int updateByPrimaryKeySelective(MessageMonitor messageMonitor) {
        final Tuple<String, List> tuple = SqlUtils.genUpdateByPrimaryKeySelective(messageMonitor);
        final Object[] args = tuple.getS().toArray(new Object[0]);
        return (int) executeAndLogSQL(tuple.getF(), args,
                (a, b) -> dependencyHolder.jdbcTemplate().update(a, b));
    }

    public Date selectMinNextSendTime() {
        String sql = "select min(next_send_time) as next_send_time from message_monitor where status = 0";
        return (Date) executeAndLogSQL(sql, null,
                (a, b) -> dependencyHolder.jdbcTemplate().queryForObject(sql, Date.class));
    }

    public List<MessageMonitor> selectSinceNextSendTime(Date nextSendTime, int status, Long minId, int limit) {
        String sql = "select * from message_monitor where next_send_time >= ? and status = ?";
        if (minId != null) {
            sql += " and id > ?";
        }
        sql += " order by next_send_time asc, id asc limit ?";
        final Object[] args = minId == null ? new Object[] {nextSendTime, status, limit} :
                new Object[] {nextSendTime, status, minId, limit};
        return (List<MessageMonitor>) executeAndLogSQL(sql, args, (a, b) -> dependencyHolder.jdbcTemplate().query(a, b,
                new BeanPropertyRowMapper<>(MessageMonitor.class)));
    }

    public MessageMonitor selectByPrimaryKey(Long id) {
        if (id == null) {
            return null;
        }
        String sql = "select * from message_monitor where id = ?";
        Object[] args = new Object[] {id};
        return (MessageMonitor) executeAndLogSQL(sql, args,
                (a, b) -> dependencyHolder.jdbcTemplate().queryForObject(a,
                        new MyBeanPropertyRowMapper<>(MessageMonitor.class), b));
    }

}
