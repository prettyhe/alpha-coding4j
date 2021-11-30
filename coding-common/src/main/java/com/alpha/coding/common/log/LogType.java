package com.alpha.coding.common.log;

/**
 * LogType
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public enum LogType {

    SERV_OUT("servOut"), // 对外提供服务
    INVOKE_OUT("invokeOut"), // 调用外部服务
    INVOKE_DB("invokeDB"), // 访问db
    INVOKE_REDIS("invokeRedis"), // 访问redis
    INVOKE_MONGODB("invokeMongoDB"), // 访问MongoDB
    DELIVER_MQ("deliverMQ"), // 投递MQ消息
    CONSUME_MQ("consumeMQ"), // 消费MQ消息
    BIZ("biz"), // 业务
    OTHER("other"); // 其它

    private String type;

    LogType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
