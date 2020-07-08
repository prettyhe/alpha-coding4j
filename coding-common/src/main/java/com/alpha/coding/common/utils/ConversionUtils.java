package com.alpha.coding.common.utils;

/**
 * ConversionUtils
 *
 * @version 1.0
 * Date: 2020/6/16
 */
public class ConversionUtils {

    private static final char[] DIGITS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * 10进制转62进制
     */
    public static String base62Encode(long num) {
        int scale = DIGITS.length;
        StringBuilder sb = new StringBuilder();
        int remainder = 0;
        while (num > scale - 1) {
            remainder = Long.valueOf(num % scale).intValue();
            sb.append(DIGITS[remainder]);
            num = num / scale;
        }
        sb.append(DIGITS[(int) num]);
        String value = sb.reverse().toString();
        return value;
    }

    /**
     * 62进制转10进制
     */
    public static long base62Decode(String str) {
        int scale = DIGITS.length;
        str = str.replace("^0*", "");
        long num = 0;
        int index = 0;
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < DIGITS.length; j++) {
                if (str.charAt(i) == DIGITS[j]) {
                    index = j;
                    break;
                }
            }
            num += (long) (index * (Math.pow(scale, str.length() - i - 1)));
        }
        return num;
    }

}
