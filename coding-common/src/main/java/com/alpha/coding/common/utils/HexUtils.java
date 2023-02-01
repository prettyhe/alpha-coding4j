package com.alpha.coding.common.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * HexUtils
 *
 * @version 1.0
 * Date: 2020/4/11
 */
public class HexUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char[] DIGITS =
            new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encodeToString(byte[] bytes) {
        char[] encodedChars = encode(bytes);
        return new String(encodedChars);
    }

    public static char[] encode(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int var4 = 0; i < l; ++i) {
            out[var4++] = DIGITS[(240 & data[i]) >>> 4];
            out[var4++] = DIGITS[15 & data[i]];
        }

        return out;
    }

    public static byte[] decode(byte[] array) throws IllegalArgumentException {
        String s = new String(array, UTF8);
        return decode(s);
    }

    public static byte[] decode(String hex) {
        return decode(hex.toCharArray());
    }

    public static byte[] decode(char[] data) throws IllegalArgumentException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        } else {
            byte[] out = new byte[len >> 1];
            int i = 0;

            for (int j = 0; j < len; ++i) {
                int f = toDigit(data[j], j) << 4;
                ++j;
                f |= toDigit(data[j], j);
                ++j;
                out[i] = (byte) (f & 255);
            }

            return out;
        }
    }

    protected static int toDigit(char ch, int index) throws IllegalArgumentException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        } else {
            return digit;
        }
    }

}
