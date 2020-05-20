package com.alpha.coding.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * CompressUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class CompressUtils {

    public static final Charset DEFAULT_CS = Charset.forName("UTF-8");

    /**
     * compress by gzip, use UTF-8 for default
     */
    public static byte[] compress(String text) throws IOException {
        return compress(text, DEFAULT_CS);
    }

    /**
     * compress by gzip
     */
    public static byte[] compress(String text, Charset charset) throws IOException {
        return text == null ? null : gzip(text.getBytes(charset == null ? DEFAULT_CS : charset));
    }

    /**
     * uncompress by gzip, use UTF-8 for default
     */
    public static String uncompress(byte[] bytes) throws IOException {
        return uncompress(bytes, DEFAULT_CS);
    }

    /**
     * uncompress by gzip
     */
    public static String uncompress(byte[] bytes, Charset charset) throws IOException {
        return bytes == null ? null : new String(unGzip(bytes), charset == null ? DEFAULT_CS : charset);
    }

    /**
     * gzip compress
     *
     * @param bytes origin bytes
     *
     * @return bytes compressed
     */
    public static byte[] gzip(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(bytes);
                gzip.finish();
            }
            final byte[] array = bos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("compress length: beforeFilter={},afterFilter={}", bytes.length, array.length);
            }
            return array;
        }
    }

    /**
     * gzip uncompress
     *
     * @param bytes origin bytes
     *
     * @return bytes uncompressed
     */
    public static byte[] unGzip(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            try (GZIPInputStream gzip = new GZIPInputStream(bis)) {
                byte[] buf = new byte[1024];
                int num = -1;
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                        bos.write(buf, 0, num);
                    }
                    bos.flush();
                    return bos.toByteArray();
                }
            }
        }
    }

}
