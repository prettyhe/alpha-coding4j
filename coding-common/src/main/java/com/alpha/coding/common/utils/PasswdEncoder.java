package com.alpha.coding.common.utils;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswdEncoder 密码编码工具
 * <p>对明文密码使用 PBKDF2 进行编码, 结果使用 HEX 编码成字符串</p>
 * <p>生成 盐 长度为 256 位, 使用 HEX 编码成字符串</p>
 * <p>
 * https://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source
 * </p>
 * <p>
 * There is a way to configure JRE to use a non-blocking entropy source by setting the following system
 * property: -Djava.security.egd=file:/dev/./urandom
 * </p>
 */
public class PasswdEncoder {

    private static final String KEY_ALGORITHMS = "PBKDF2WithHmacSHA1";
    private static final String ALGORITHMS_SHA1PRNG = "SHA1PRNG";

    private static final int PBE_ITERATION_COUNT = 1000;

    private static final int KEY_BIT_LENGTH = 256;

    private static final int SALT_BIT_LENGTH = 256;

    /**
     * 对明文密码编码， 结果为 hex 编码字符串
     *
     * @param passwd  密码明文
     * @param saltHex hex编码后的盐
     */
    public static String encode(String passwd, String saltHex) throws GeneralSecurityException {
        return HexUtils.encodeToString(encode(passwd, HexUtils.decode(saltHex)));
    }

    /**
     * 对明文密码编码，
     *
     * @param passwd    密码明文
     * @param saltBytes 盐
     */
    public static byte[] encode(String passwd, byte[] saltBytes) throws GeneralSecurityException {
        /* 对密码使用 PBKDF2 算法进行编码 */
        PBEKeySpec pbeKeySpec = new PBEKeySpec(passwd.toCharArray(), saltBytes,
                PBE_ITERATION_COUNT, KEY_BIT_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHMS);
        SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
        return secretKey.getEncoded();
    }

    /**
     * 生成 32 字节随机盐
     */
    public static byte[] genSalt() throws GeneralSecurityException {
        SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHMS_SHA1PRNG);
        byte[] resultBytes = secureRandom.generateSeed(SALT_BIT_LENGTH / 8);
        secureRandom.nextBytes(resultBytes);
        return resultBytes;
    }

    /**
     * 生成 32 字节随机盐, hex 编码成字符串
     */
    public static String genSaltHex() throws GeneralSecurityException {
        return HexUtils.encodeToString(genSalt());
    }

    /**
     * 判断密文参数是否是从明文参数编码而来
     */
    public static boolean isEncodedOfPlain(String encoded, String plainText, String saltHex)
            throws GeneralSecurityException {
        return encode(plainText, saltHex).equals(encoded);
    }

}
