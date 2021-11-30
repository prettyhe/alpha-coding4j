package com.alpha.coding.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AESUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class AESUtils {

    private static final String ASCII = "ASCII";
    private static final String AES = "AES";
    private static final String UTF8 = "UTF-8";

    /**
     * AES-128-ECB
     */
    private static final String AES_MODE = "AES/ECB/PKCS5Padding"; // "算法/模式/补码方式"

    /**
     * 将经过aes加密过的byte[]解密
     *
     * @param bytes  加密后的byte[]
     * @param strKey 用于加/解密的salt
     *
     * @return 解密串
     *
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String aesDecrypt(byte[] bytes, String strKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = Arrays.copyOf(strKey.getBytes(ASCII), 16);
        SecretKey key = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] cipherTextBytes = cipher.doFinal(bytes);
        return new String(cipherTextBytes);
    }

    /**
     * 对字符串使用aes加密
     *
     * @param str    原始字符串
     * @param strKey 用于加/解密的salt
     *
     * @return 加密字节数组
     *
     * @throws UnsupportedEncodingException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] aesEncrypt(String str, String strKey)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = Arrays.copyOf(strKey.getBytes(ASCII), 16);
        SecretKey key = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cleartext = str.getBytes(UTF8);
        byte[] cipherTextBytes = cipher.doFinal(cleartext);
        return cipherTextBytes;
    }

    /**
     * 将经过aes加密过的byte[]解密，使用AES-128-ECB算法
     *
     * @param bytes  加密后的byte[]
     * @param strKey 用于加/解密的salt，最大有效长度为16位
     *
     * @return 解密字节数组
     */
    public static byte[] aesECBDecrypt(byte[] bytes, String strKey) throws Exception {
        try {
            byte[] keyBytes = Arrays.copyOf(strKey.getBytes(ASCII), 16);
            SecretKey key = new SecretKeySpec(keyBytes, AES);
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherTextBytes = cipher.doFinal(bytes);
            return cipherTextBytes;
        } catch (UnsupportedEncodingException e) {
            throw new Exception("不支持的编码");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (InvalidKeyException e) {
            throw new Exception("非法加密KEY");
        } catch (NoSuchPaddingException e) {
            throw e;
        } catch (BadPaddingException e) {
            throw e;
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        }
    }

    /**
     * 对字符串使用aes加密，使用AES-128-ECB算法
     *
     * @param bytes  原始bytes
     * @param strKey 用于加/解密的salt，最大有效长度为16位
     *
     * @return 加密字节数组
     */
    public static byte[] aesECBEncrypt(byte[] bytes, String strKey) throws Exception {
        try {
            byte[] keyBytes = Arrays.copyOf(strKey.getBytes(ASCII), 16);
            SecretKey key = new SecretKeySpec(keyBytes, AES);
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherTextBytes = cipher.doFinal(bytes);
            return cipherTextBytes;
        } catch (UnsupportedEncodingException e) {
            throw new Exception("不支持的编码");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此加密算法");
        } catch (InvalidKeyException e) {
            throw new Exception("非法加密KEY");
        } catch (NoSuchPaddingException e) {
            throw e;
        } catch (BadPaddingException e) {
            throw e;
        } catch (IllegalBlockSizeException e) {
            throw new Exception("明文长度非法");
        }
    }

}
