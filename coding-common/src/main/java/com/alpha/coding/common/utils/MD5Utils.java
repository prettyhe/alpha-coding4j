package com.alpha.coding.common.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5Utils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class MD5Utils {

    private static final char[] hexDigits =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String md5(Object... params) {
        if (params == null || params.length <= 0) {
            throw new RuntimeException("null object");
        }
        StringBuilder sb = new StringBuilder();
        for (Object o : params) {
            sb.append(o.toString());
        }
        return md5(sb.toString());
    }

    public static String md5(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            byte[] dataBytes = new byte[1024];
            int n = 0;
            while ((n = is.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, n);
            }
            byte[] bytes = md.digest();
            return md5byte2Str(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bs = str.getBytes(StandardCharsets.UTF_8);
            md.reset();
            md.update(bs);
            byte[] md5Bytes = md.digest();
            return md5byte2Str(md5Bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(bytes);
            byte[] md5Bytes = md.digest();
            return md5byte2Str(md5Bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5byte2Str(byte[] md5bytes) {
        char[] md5Chars = new char[32];
        int k = 0;
        for (byte b : md5bytes) {
            md5Chars[k++] = hexDigits[b >>> 4 & 0xf];
            md5Chars[k++] = hexDigits[b & 0xf];
        }
        return new String(md5Chars);
    }

}
