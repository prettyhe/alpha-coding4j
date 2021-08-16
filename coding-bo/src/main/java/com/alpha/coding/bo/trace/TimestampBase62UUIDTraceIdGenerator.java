package com.alpha.coding.bo.trace;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import lombok.Getter;

/**
 * TimestampBase62UUIDTraceIdGenerator(随机串 + base62编码时间戳 + 随机串)
 *
 * @version 1.0
 * Date: 2020/7/20
 */
public class TimestampBase62UUIDTraceIdGenerator implements TraceIdGenerator {

    private static final char[] DIGITS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    /**
     * 最长长度
     */
    private static final int LENGTH = 20;

    @Getter
    private static final TimestampBase62UUIDTraceIdGenerator instance = new TimestampBase62UUIDTraceIdGenerator();

    @Override
    public String traceId() {
        String timestamp = base62Encode(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        final int start = ThreadLocalRandom.current().nextInt(0, uuid.length() - (LENGTH - timestamp.length()));
        final String uuidStr = uuid.substring(start, start + (LENGTH - timestamp.length()));
        final int prefixLength = (LENGTH + 1) / 2 - timestamp.length();
        return uuidStr.substring(0, Math.max(prefixLength, 0))
                + timestamp + uuidStr.substring(Math.max(prefixLength, 0));
    }

    /**
     * 反解时间戳
     */
    public long parseTimestamp(String traceId) {
        String timestamp = base62Encode(System.currentTimeMillis());
        final int prefixLength = (LENGTH + 1) / 2 - timestamp.length();
        return base62Decode(traceId.substring(Math.max(prefixLength, 0)).substring(0, timestamp.length()));
    }

    /**
     * 10进制转62进制
     */
    private String base62Encode(long num) {
        int scale = DIGITS.length;
        StringBuilder sb = new StringBuilder();
        int remainder = 0;
        while (num > scale - 1) {
            remainder = Long.valueOf(num % scale).intValue();
            sb.append(DIGITS[remainder]);
            num = num / scale;
        }
        sb.append(DIGITS[(int) num]);
        return sb.reverse().toString();
    }

    /**
     * 62进制转10进制
     */
    private long base62Decode(String str) {
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
