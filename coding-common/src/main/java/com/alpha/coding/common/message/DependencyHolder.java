package com.alpha.coding.common.message;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alpha.coding.common.message.publish.MessagePublishAdaptor;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * DependencyHolder
 *
 * @version 1.0
 * Date: 2021/9/8
 */
@Data
@Slf4j
public class DependencyHolder implements InitializingBean {

    /**
     * 业务组
     */
    @Getter(AccessLevel.PRIVATE)
    private volatile String bizGroup;

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * redis
     */
    private RedisTemplate redisTemplate;

    /**
     * 消息发送适配器
     */
    private MessagePublishAdaptor messagePublishAdaptor;

    /**
     * JdbcTemplate
     */
    @Getter(AccessLevel.PRIVATE)
    private volatile JdbcTemplate jdbcTemplate;

    public JdbcTemplate jdbcTemplate() {
        if (jdbcTemplate == null && dataSource != null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    public String bizGroup() {
        if (bizGroup == null) {
            bizGroup = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        }
        return bizGroup;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("bizGroup => {}, dataSource => {}, redisTemplate => {}, messagePublishAdaptor => {}",
                bizGroup, dataSource, redisTemplate, messagePublishAdaptor);
    }
}
