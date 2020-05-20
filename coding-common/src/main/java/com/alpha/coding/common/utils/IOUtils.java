/**
 * Copyright
 */
package com.alpha.coding.common.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IOUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class IOUtils {

    /**
     * The Unix line separator string.
     */
    public static final String LINE_SEPARATOR_UNIX = "\n";
    /**
     * The Windows line separator string.
     */
    public static final String LINE_SEPARATOR_WINDOWS = "\r\n";

    private static final int BUF_SIZE = 1024;

    /**
     * 二进制方式读流
     *
     * @param inStream 输入流
     *
     * @return 返回读到的字节数
     *
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inStream) throws IOException {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // 创建一个Buffer字符串
            byte[] buffer = new byte[BUF_SIZE];
            // 每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len = 0;
            // 使用一个输入流从buffer里把数据读取出来
            while ((len = inStream.read(buffer)) != -1) {
                // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            // 把outStream里的数据写入内存
            return outStream.toByteArray();
        }
    }

    /**
     * 读取Reader流中的所有行,遇到空行结束
     *
     * @param input 输入
     *
     * @return 读到的行
     *
     * @throws Exception
     */
    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line = readline(reader);
        while (StringUtils.isNotEmpty(line)) {
            list.add(line);
            line = readline(reader);
        }
        return list;
    }

    /**
     * 读取输入流中的所有非空行
     *
     * @param is                             输入流
     * @param maxContinuousEmptyLinesAllowed 允许的连续最大空行数，若超过此数的空行，则读取截止
     * @param charset                        字符集,e.g. UTF-8
     *
     * @return 读到的行
     *
     * @throws IOException
     */
    public static List<String> readLines(InputStream is, int maxContinuousEmptyLinesAllowed, String charset)
            throws IOException {
        List<String> lines = Lists.newArrayList();
        int emptyLineCnt = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            String line = readline(reader);
            boolean empty = StringUtils.isEmpty(line);
            while (!empty || emptyLineCnt < maxContinuousEmptyLinesAllowed) {
                if (empty) {
                    emptyLineCnt++;
                } else {
                    emptyLineCnt = 0;
                    lines.add(line);
                }
                line = readline(reader);
                empty = StringUtils.isEmpty(line);
            }
        }
        return lines;
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * 读取BufferedReader流中的一行，行分隔符为'\n'或'\r\n'
     *
     * @param reader BufferedReader
     *
     * @return 读取一行的值
     *
     * @throws Exception
     */
    public static String readline(BufferedReader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int c = 0;
        while ((c = reader.read()) != '\n' && c != -1) {
            if (c == '\r') {
                continue;
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    /**
     * 释放流
     *
     * @param streams 流
     */
    public static void releaseStream(Object... streams) {
        for (Object stream : streams) {
            if (stream != null) {
                if (stream instanceof InputStream) {
                    try {
                        ((InputStream) stream).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (stream instanceof OutputStream) {
                    try {
                        ((OutputStream) stream).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                stream = null;
            }
        }
    }

    /**
     * 从classpath中读取文件，默认去掉换行符
     *
     * @param filename 文件名
     * @param charset  字符集
     *
     * @return 读取文件的内容
     *
     * @throws IOException
     */
    public static String readFileFromSrc(String filename, Charset charset) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            return readFromInputStream(is, charset, false);
        }
    }

    /**
     * 从InputStream中读取内容
     *
     * @param is                InputStream
     * @param charset           字符集
     * @param withLineSeparator 是否携带换行符
     *
     * @return 读取InputStream的内容
     *
     * @throws IOException
     */
    public static String readFromInputStream(InputStream is, Charset charset, boolean withLineSeparator)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                if (withLineSeparator) {
                    sb.append(LINE_SEPARATOR_UNIX);
                }
            }
        }
        return sb.toString();
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
     * 按行读取流
     *
     * @param is       输入流
     * @param config   配置
     * @param lineCall 回调
     *
     * @throws IOException
     */
    public static void readLineByLine(InputStream is, ReadConfig config, Consumer<String> lineCall) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is, config.getCharsetName()), config.getBufferSize())) {
            String line = readline(reader);
            int emptyLineCnt = 0;
            while (emptyLineCnt <= config.getMaxContinuousEmptyLinesAllowed()) {
                emptyLineCnt = StringUtils.isEmpty(line) ? emptyLineCnt + 1 : 0;
                lineCall.accept(line);
                line = readline(reader);
            }
        }
    }

    /**
     * 按行读取文件
     *
     * @param file     文件
     * @param config   配置
     * @param lineCall 回调
     *
     * @throws IOException
     */
    public static void readFileLineByLine(File file, ReadConfig config, Consumer<String> lineCall) throws IOException {
        try (FileInputStream is = openInputStream(file)) {
            readLineByLine(is, config, lineCall);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class ReadConfig {
        private int bufferSize = 2 * 1024 * 1024;
        private String charsetName = "UTF-8";
        private int maxContinuousEmptyLinesAllowed = 5;

        public static ReadConfig getDefault() {
            return new ReadConfig();
        }
    }

    public static String joinPath(String path1, String path2) {
        return (path1.endsWith(File.separator) ? path1 : (path1 + File.separator)) + path2;
    }

}
