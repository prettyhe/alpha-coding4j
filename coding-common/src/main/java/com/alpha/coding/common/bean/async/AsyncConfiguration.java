package com.alpha.coding.common.bean.async;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AsyncConfiguration
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Data
@Accessors(chain = true)
public class AsyncConfiguration {

    private String identify = UUID.randomUUID().toString();
    private int coreExeSize = Runtime.getRuntime().availableProcessors() * 2;
    private int queueCapacity = 1000;
    private int batchSize = 100;
    private int sleepMillis = 50;
    private int monitorMaxNopTimes = 15;
    private long monitorNopSleepMillis = 20000;
    private BlockingQueue queue;

}
