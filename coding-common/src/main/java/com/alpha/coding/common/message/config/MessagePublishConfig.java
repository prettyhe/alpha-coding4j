package com.alpha.coding.common.message.config;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * MessagePublishConfig
 *
 * @version 1.0
 * Date: 2021/7/3
 */
@Data
@Accessors(chain = true)
public class MessagePublishConfig implements Serializable {

    /**
     * 最大尝试次数
     */
    private Integer maxTryTimes;
    /**
     * 重试间隔(秒)
     */
    private Integer retryInterval;
    /**
     * 是否需要回执
     */
    private boolean needReceipt;
    /**
     * 定时发送时间
     */
    private Date delaySendTime;
    /**
     * 发送结果监听器
     */
    private String publishListener;
    /**
     * 冗余参数
     */
    private volatile Map<String, Object> tmpData;

    public MessagePublishConfig putIntoTmpData(String key, Object val) {
        if (tmpData == null) {
            synchronized(this) {
                if (tmpData == null) {
                    tmpData = new HashMap<>();
                }
            }
        }
        tmpData.put(key, val);
        return this;
    }

    public Object getFromTmpData(String key) {
        return tmpData == null ? null : tmpData.get(key);
    }

    public static MessagePublishConfig withListener(String publishListener) {
        return new MessagePublishConfig().setPublishListener(publishListener);
    }
}
