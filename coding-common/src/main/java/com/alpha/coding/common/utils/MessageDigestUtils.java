package com.alpha.coding.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MessageDigestUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MessageDigestUtils {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";
    public static final String MD5 = "MD5";

    public static String encode(String algorithm, byte[] bytes) throws NoSuchAlgorithmException {
        if (algorithm == null || bytes == null) {
            return null;
        }
        byte[] digest = digest(algorithm, bytes);
        return formatToStr(digest);
    }

    public static String encode(String algorithm, String str) throws NoSuchAlgorithmException {
        return encode(algorithm, str.getBytes());
    }

    private static byte[] digest(String algorithm, byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        //        messageDigest.reset();
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    private static String formatToStr(byte[] bytes) {
        int len = bytes.length;
        StringBuilder sb = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            sb.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            sb.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return sb.toString();
    }

}
