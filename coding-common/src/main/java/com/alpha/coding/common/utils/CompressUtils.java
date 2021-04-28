package com.alpha.coding.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

/**
 * CompressUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
@Slf4j
public class CompressUtils {

    public static final Charset DEFAULT_CS = StandardCharsets.UTF_8;

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
                log.debug("compress length: before={},after={}", bytes.length, array.length);
            }
            return array;
        }
    }

    /**
     * gzip uncompress
     *
     * @param bytes origin bytes
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

    /**
     * zip compress
     *
     * @param srcFile    source file
     * @param zip        zip file base name, name of srcFile while null
     * @param fileFilter fileFilter filter for file
     * @return bytes compressed
     */
    public static byte[] zip(File srcFile, String zip, Predicate<File> fileFilter) throws IOException {
        if (srcFile == null || !srcFile.exists()) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                try (BufferedOutputStream bfos = new BufferedOutputStream(zos)) {
                    zip(zos, bfos, srcFile, zip == null ? srcFile.getName() : zip, fileFilter);
                }
            }
            return bos.toByteArray();
        }
    }

    /**
     * zip压缩
     *
     * @param out        zip压缩输出流
     * @param bos        带缓冲的输出流
     * @param srcFile    源文件
     * @param zip        压缩地址
     * @param fileFilter 文件过滤器
     */
    private static void zip(ZipOutputStream out, BufferedOutputStream bos, File srcFile, String zip,
                            Predicate<File> fileFilter)
            throws IOException {
        // 如果路径为目录（文件夹）
        if (srcFile.isDirectory()) {
            // 取出文件夹中的文件（或子文件夹）
            File[] fileList = srcFile.listFiles();
            // 如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
            if (fileList == null || fileList.length == 0) {
                if (fileFilter == null || fileFilter.test(srcFile)) {
                    out.putNextEntry(new ZipEntry(zip + File.separator));
                }
            } else {
                // 如果文件夹不为空，则递归调用zip，文件夹中的每一个文件（或文件夹）进行压缩
                for (File file : fileList) {
                    zip(out, bos, file, zip + File.separator + file.getName(), fileFilter);
                }
            }
        } else {
            if (fileFilter == null || fileFilter.test(srcFile)) {
                // 如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
                out.putNextEntry(new ZipEntry(zip));
                try (FileInputStream fos = new FileInputStream(srcFile)) {
                    try (BufferedInputStream bis = new BufferedInputStream(fos)) {
                        int tag;
                        // 将源文件写入到zip文件中
                        while ((tag = bis.read()) != -1) {
                            bos.write(tag);
                        }
                    }
                }
            }
        }
    }

    /**
     * 解压文件到指定目录
     *
     * @param zipFile zip压缩文件
     * @param descDir 目标目录
     * @return 解压出来的文件
     */
    public static List<File> unzip(File zipFile, String descDir) throws IOException {
        final List<File> files = new ArrayList<>();
        ZipFile usedZipFile = new ZipFile(zipFile);
        for (Enumeration<? extends ZipEntry> entries = usedZipFile.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = entries.nextElement();
            File file = new File(descDir + File.separator + entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                try (InputStream inputStream = usedZipFile.getInputStream(entry)) {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        int len;
                        byte[] bytes = new byte[1024];
                        while ((len = inputStream.read(bytes)) > 0) {
                            outputStream.write(bytes, 0, len);
                        }
                        outputStream.flush();
                    }
                }
                files.add(file);
            }
        }
        return files;
    }

}
