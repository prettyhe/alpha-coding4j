package com.alpha.coding.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * SignUtil
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class SignUtil {

    private final static String CHARSET = "UTF-8";
    // 属性间的分隔符
    private final static String PROPERTY_SPLIT = "&";
    // 键值间的分隔符
    private final static String KV_SPLIT = "=";

    public static String encryptData(String data, String aesKey) throws Exception {
        return getContent(base64Encode(aesEncrypt(getContentBytes(aesKey), getContentBytes(data))));
    }

    public static String decryptData(String data, String aesKey) throws Exception {
        return getContent(
                aesDecrypt(getContentBytes(aesKey), base64Decode(getContentBytes(data))));
    }

    public static String sign(Map<String, String> propertyMap, String appSecret)
            throws UnsupportedEncodingException {
        String sortedStr = getSortedStr(propertyMap, appSecret);
        return signWithMd5(sortedStr);
    }

    public static boolean verify(Map<String, String> propertyMap, String sign, String appSecret)
            throws Exception {
        String sortedStr = getSortedStr(propertyMap, appSecret);
        return sign.equals(signWithMd5(sortedStr));
    }

    private static byte[] base64Encode(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encode(data);
    }

    private static byte[] base64Decode(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(data);
    }

    private static byte[] aesEncrypt(byte[] key, byte[] data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] aesDecrypt(byte[] key, byte[] data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static String signWithMd5(String data) throws UnsupportedEncodingException {
        return DigestUtils.md5Hex(getContentBytes(data));
    }

    private static byte[] getContentBytes(String content) throws UnsupportedEncodingException {
        return content.getBytes(CHARSET);
    }

    private static String getContent(byte[] bytes) throws UnsupportedEncodingException {
        return new String(bytes, CHARSET);
    }

    private static String getSortedStr(Map<String, String> properties, String appSecret) {
        TreeMap<String, String> treeMap = new TreeMap<>();

        Iterator<Entry<String, String>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            treeMap.put(entry.getKey(), entry.getValue());
        }
        treeMap.put("appSecret", appSecret);

        StringBuilder sb = new StringBuilder(128);
        it = treeMap.entrySet().iterator();
        boolean first = true;
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (first) {
                sb.append(entry.getKey()).append(KV_SPLIT == null ? "" : KV_SPLIT)
                        .append(entry.getValue());
                first = false;
            } else {
                sb.append(PROPERTY_SPLIT == null ? "" : PROPERTY_SPLIT).append(entry.getKey())
                        .append(KV_SPLIT == null ? "" : KV_SPLIT).append(entry.getValue());
            }
        }

        return sb.toString();
    }
}
