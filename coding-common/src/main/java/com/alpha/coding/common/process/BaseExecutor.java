package com.alpha.coding.common.process;

/**
 * BaseExecutor
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public interface BaseExecutor {

    /**
     * Process执行体
     *
     * @param args 执行参数
     *
     * @throws Exception
     */
    void execute(String... args) throws Exception;

}
