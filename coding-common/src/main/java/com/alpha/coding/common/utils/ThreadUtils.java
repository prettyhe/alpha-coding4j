package com.alpha.coding.common.utils;

/**
 * ThreadUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class ThreadUtils {

    public static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    public static void trySleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
