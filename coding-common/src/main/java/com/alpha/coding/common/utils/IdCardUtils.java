package com.alpha.coding.common.utils;

import java.util.Date;

/**
 * IdCardUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class IdCardUtils {

    private static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * 获取身份证出生日期
     */
    public static Date getBirthday(String idCard) {
        String birth = null;
        if (idCard.length() == 18) {
            birth = idCard.substring(6, 14);
        } else if (idCard.length() == 15) {
            birth = "19" + idCard.substring(6, 12);
        } else {
            throw new RuntimeException("illegal idcard " + idCard);
        }
        return DateUtils.parse(birth, DATE_FORMAT);
    }

    /**
     * 获取身份证年龄，算足的(到天)
     */
    public static int getAge(String idCard) {
        return DateUtils.age(getBirthday(idCard));
    }

    /**
     * 根据身份证计算到指定日期的年龄，算足的(到天)
     */
    public static int calAge(String idCard, Date specifyDate) {
        return DateUtils.age(specifyDate, getBirthday(idCard));
    }

}
