package com.alpha.coding.common.utils;

import com.alibaba.fastjson.JSON;

/**
 * PrintUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class PrintUtils {

    public static void printJson(Object... objects) {
        for (Object object : objects) {
            System.out.println(JSON.toJSONString(object));
        }
    }

    public static void printJackson(Object... objects) {
        for (Object object : objects) {
            System.out.println(JacksonUtils.toJson(object));
        }
    }

    public static void printJsonDefaultDateFormat(Object target) {
        System.out.println(JSON.toJSONStringWithDateFormat(target, DateUtils.DEFAULT_FORMAT));
    }

}
