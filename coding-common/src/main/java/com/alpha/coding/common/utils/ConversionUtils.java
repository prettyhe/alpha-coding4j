package com.alpha.coding.common.utils;

import java.math.BigInteger;

/**
 * ConversionUtils
 *
 * @version 1.0
 * Date: 2020/6/16
 */
public class ConversionUtils {

    private static final char[] CODEC_DIGITS = new char[] {
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
        return base62EncodeBigInt(new BigInteger(String.valueOf(num)));
    }

    /**
     * 10进制转62进制，支持大整型
     */
    public static String base62EncodeBigInt(BigInteger num) {
        int scale = CODEC_DIGITS.length;
        StringBuilder sb = new StringBuilder();
        int remainder = 0;
        BigInteger bench = new BigInteger(String.valueOf(scale - 1));
        BigInteger scaleBI = new BigInteger(String.valueOf(scale));
        while (num.compareTo(bench) > 0) {
            remainder = num.remainder(scaleBI).intValue();
            sb.append(CODEC_DIGITS[remainder]);
            num = num.divide(scaleBI);
        }
        sb.append(CODEC_DIGITS[num.intValue()]);
        return sb.reverse().toString();
    }

    /**
     * 62进制转10进制，可能会导致整数溢出
     */
    public static long base62Decode(String str) {
        return base62DecodeToBigInt(str).longValue();
    }

    /**
     * 62进制转10进制，兼容长整型
     */
    public static BigInteger base62DecodeToBigInt(String str) {
        int scale = CODEC_DIGITS.length;
        str = str.replace("^0*", "");
        BigInteger num = new BigInteger("0");
        int index = 0;
        for (int i = 0; i < str.length(); i++) {
            for (int j = 0; j < CODEC_DIGITS.length; j++) {
                if (str.charAt(i) == CODEC_DIGITS[j]) {
                    index = j;
                    break;
                }
            }
            num = num.add(new BigInteger(String.valueOf(index))
                    .multiply(new BigInteger(String.valueOf(scale)).pow(str.length() - i - 1)));
        }
        return num;
    }

}
