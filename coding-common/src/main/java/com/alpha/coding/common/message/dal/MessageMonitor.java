package com.alpha.coding.common.message.dal;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.alpha.coding.common.message.config.MessagePublishConfig;

import lombok.Data;

@Data
@Table(name = "message_monitor")
@Entity(name = "message_monitor")
public class MessageMonitor implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "biz_no")
    private String bizNo;
    @Column(name = "topic")
    private String topic;
    @Column(name = "tag")
    private String tag;
    @Column(name = "content")
    private String content;
    @Column(name = "msg_id")
    private String msgId;
    @Column(name = "next_send_time")
    private Date nextSendTime;
    @Column(name = "status")
    private Integer status;
    @Column(name = "need_receipt")
    private Integer needReceipt;
    @Column(name = "try_times")
    private Integer tryTimes;
    @Column(name = "max_try_times")
    private Integer maxTryTimes;
    @Column(name = "retry_interval")
    private Integer retryInterval;
    @Column(name = "publish_listener")
    private String publishListener;
    @Column(name = "send_time")
    private Date sendTime;
    @Column(name = "receipt_time")
    private Date receiptTime;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    private transient MessagePublishConfig publishConfig;

}
