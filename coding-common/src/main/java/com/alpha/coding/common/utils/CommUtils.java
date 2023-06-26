package com.alpha.coding.common.utils;

import java.util.UUID;

/**
 * CommUtils
 *
 * @version 1.0
 * Date: 2021/11/12
 */
public class CommUtils {

    private static final String CLIENT_ID = uuidUpperCase();

    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }

    public static String uuidUpperCase() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static String clientId() {
        return CLIENT_ID;
    }

}
