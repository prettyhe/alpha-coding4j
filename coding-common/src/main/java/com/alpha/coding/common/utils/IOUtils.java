package com.alpha.coding.common.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    /**
     * default buffer size
     */
    private static final int BUF_SIZE = 4096;

    /**
     * 二进制方式读流
     *
     * @param inStream 输入流
     * @return 读到的字节
     * @throws IOException 抛出IOException
     */
    public static byte[] readInputStream(InputStream inStream) throws IOException {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
            // 创建一个Buffer字符串
            byte[] buffer = new byte[BUF_SIZE];
            // 每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len;
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
     * @return 读到的行
     * @throws Exception 抛出IOException
     */
    public static List<String> readlines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<>();
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
     * @return 读到的行
     * @throws IOException 抛出IOException
     */
    public static List<String> readLines(InputStream is, int maxContinuousEmptyLinesAllowed, String charset)
            throws IOException {
        List<String> lines = new ArrayList<>();
        int emptyLineCnt = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        String line = readline(reader);
        boolean empty = StringUtils.isEmpty(line);
        while (!empty || emptyLineCnt < maxContinuousEmptyLinesAllowed) {
            if (empty) {
                emptyLineCnt++;
            } else {
                emptyLineCnt = 0;
                lines.add(line);
            }
            line = IOUtils.readline(reader);
            empty = StringUtils.isEmpty(line);
        }
        return lines;
    }

    /**
     * 转换为BufferedReader
     *
     * @param reader reader
     * @return BufferedReader
     */
    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * 读取BufferedReader流中的一行，行分隔符为'\n'或'\r\n'
     *
     * @param reader BufferedReader
     * @return 读取一行的值
     * @throws IOException 抛出IOException
     */
    public static String readline(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
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
            if (stream instanceof Closeable) {
                close((Closeable) stream);
            }
        }
    }

    /**
     * 关闭资源
     *
     * @param closeables 可关闭的资源
     */
    public static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }

    /**
     * 从classpath中读取文件，默认去掉换行符
     *
     * @param filename 文件名
     * @param charset  字符集
     * @return 读取文件的内容
     * @throws IOException 抛出IOException
     */
    public static String readFileFromSrc(String filename, Charset charset) throws IOException {
        InputStream is = null;
        try {
            is = IOUtils.class.getClassLoader().getResourceAsStream(filename);
            return readFromInputStream(is, charset, false);
        } finally {
            releaseStream(is);
        }
    }

    /**
     * 从InputStream中读取内容
     *
     * @param is                InputStream
     * @param charset           字符集
     * @param withLineSeparator 是否携带换行符
     * @return 读取InputStream的内容
     * @throws IOException 抛出IOException
     */
    public static String readFromInputStream(InputStream is, Charset charset, boolean withLineSeparator)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                if (withLineSeparator) {
                    sb.append(LINE_SEPARATOR_UNIX);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 打开文件获取文件输入流
     *
     * @param file 文件
     * @return 文件输入流
     * @throws IOException 抛出IOException
     */
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    /**
     * 打开文件获取文件输入流
     *
     * @param filePath 文件全路径
     * @return 文件输入流
     * @throws IOException 抛出IOException
     */
    public static FileInputStream openInputStream(String filePath) throws IOException {
        return openInputStream(new File(filePath));
    }

    /**
     * 打开文件获取文件输入流
     *
     * @param dirPath  文件所在目录路径
     * @param fileName 文件名
     * @return 文件输入流
     * @throws IOException 抛出IOException
     */
    public static FileInputStream openInputStream(String dirPath, String fileName) throws IOException {
        return openInputStream(joinPath(dirPath, fileName));
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param file 文件
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param file   文件
     * @param append 是否以追加方式
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }

            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }
        return new FileOutputStream(file, append);
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param dirPath  文件所在目录
     * @param fileName 文件名
     * @param append   是否以追加方式
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(String dirPath, String fileName, boolean append)
            throws IOException {
        return openOutputStream(new File(joinPath(dirPath, fileName)), append);
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param dirPath  文件所在目录
     * @param fileName 文件名
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(String dirPath, String fileName) throws IOException {
        return openOutputStream(new File(joinPath(dirPath, fileName)));
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param filePath 文件全路径
     * @param append   是否以追加方式
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(String filePath, boolean append) throws IOException {
        return openOutputStream(new File(filePath), append);
    }

    /**
     * 打开文件获取文件输出流
     *
     * @param filePath 文件全路径
     * @return 文件输出流
     * @throws IOException 抛出IOException
     */
    public static FileOutputStream openOutputStream(String filePath) throws IOException {
        return openOutputStream(new File(filePath));
    }

    /**
     * 按行读取流
     *
     * @param is     输入流
     * @param config 配置
     * @param call   回调
     * @throws IOException 抛出IOException
     */
    public static void readLineByLine(InputStream is, ReadConfig config, LineCall call) throws IOException {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(is, config.getCharsetName()), config.getBufferSize())) {
            String line = readline(reader);
            int emptyLineCnt = 0;
            while (emptyLineCnt <= config.getMaxContinuousEmptyLinesAllowed()) {
                emptyLineCnt = StringUtils.isEmpty(line) ? emptyLineCnt + 1 : 0;
                call.handle(line);
                line = readline(reader);
            }
        }
    }

    /**
     * 按行读取文件
     *
     * @param file   文件
     * @param config 配置
     * @param call   回调
     * @throws IOException 抛出IOException
     */
    public static void readFileLineByLine(File file, ReadConfig config, LineCall call) throws IOException {
        try (FileInputStream is = openInputStream(file)) {
            readLineByLine(is, config, call);
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

    public static interface LineCall {
        void handle(String line);
    }

    /**
     * 连接路径
     *
     * @param path1 路径1
     * @param path2 路径2
     * @return 新路径
     */
    public static String joinPath(String path1, String path2) {
        final int length = File.separator.length();
        if (length > 0) {
            while (path1.endsWith(File.separator)) {
                path1 = path1.substring(0, path1.length() - length);
            }
            while (path2.startsWith(File.separator)) {
                path2 = path2.substring(length);
            }
        }
        return path1 + File.separator + path2;
    }

    /**
     * 连接路径
     *
     * @param basePath 基础路径
     * @param paths    附加路径
     * @return 新路径
     */
    public static String joinPath(String basePath, String... paths) {
        if (paths == null) {
            return basePath;
        }
        String result = basePath;
        for (String path : paths) {
            if (path == null) {
                continue;
            }
            result = joinPath(result, path);
        }
        return result;
    }

    /**
     * 初始化目录，先清空，再创建
     *
     * @param dirPath 目录路径
     * @return 目录文件
     */
    public static File initDir(String dirPath) {
        cleanPath(dirPath);
        File file = new File(dirPath);
        file.mkdirs();
        return file;
    }

    /**
     * 获取文件绝对路径
     *
     * @param file 文件
     * @return 文件路径
     */
    public static String getAbsolutePath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 创建临时随机目录(当前java临时目录/uuid)
     */
    public static String makeRandomTmpDir() {
        final String path = joinPath(System.getProperty("java.io.tmpdir"), CommUtils.uuid());
        File dir = initDir(path);
        return getAbsolutePath(dir);
    }

    /**
     * 清理文件
     *
     * @param path 路径
     */
    public static boolean cleanPath(String path) {
        File file = new File(path);
        if (!file.exists()) {//判断是否待删除目录是否存在
            return false;
        }
        if (!file.isDirectory()) {
            file.delete();
            return true;
        }
        String[] content = file.list(); // 取得当前目录下所有文件和文件夹
        if (content != null) {
            for (String name : content) {
                File temp = new File(path, name);
                if (temp.isDirectory()) { // 判断是否是目录
                    try {
                        cleanPath(temp.getCanonicalPath()); // 递归调用，删除目录里的内容
                    } catch (IOException e) {
                        cleanPath(temp.getAbsolutePath()); // 递归调用，删除目录里的内容
                    }
                } else {
                    if (!temp.delete()) {//直接删除文件
                        System.out.println("Failed to delete " + name);
                    }
                }
            }
        }
        file.delete(); // 删除目录
        return true;
    }

    /**
     * 清理文件
     *
     * @param file 文件
     */
    public static boolean deleteFile(File file) {
        String canonicalPath = null;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        return cleanPath(canonicalPath);
    }

    /**
     * 写入数据到文件
     *
     * @param file 文件
     * @param data 内容
     * @throws IOException 抛出IOException
     */
    public static void writeByteArrayToFile(final File file, final byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    /**
     * 写入数据到文件
     *
     * @param file   文件
     * @param data   内容
     * @param append 是否以追究形式
     * @throws IOException 抛出IOException
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append)
            throws IOException {
        writeByteArrayToFile(file, data, 0, data.length, append);
    }

    /**
     * 写入数据到文件
     *
     * @param file 文件
     * @param data 内容
     * @param off  偏移量
     * @param len  长度
     * @throws IOException 抛出IOException
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len)
            throws IOException {
        writeByteArrayToFile(file, data, off, len, false);
    }

    /**
     * 写入数据到文件
     *
     * @param file   文件
     * @param data   内容
     * @param off    偏移量
     * @param len    长度
     * @param append 是否以追加形式
     * @throws IOException 抛出IOException
     */
    public static void writeByteArrayToFile(final File file, final byte[] data, final int off, final int len,
                                            final boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data, off, len);
        }
    }

    /**
     * 文件写入到目录
     *
     * @param bytes    文件内容
     * @param fileName 文件名
     * @param dirPath  文件保存目录
     * @throws IOException 抛出IOException
     */
    public static void writeFileToPath(byte[] bytes, String fileName, String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.setWritable(true, false);
            dir.mkdirs();
        }
        File file = new File(joinPath(dirPath, fileName));
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        writeByteArrayToFile(file, bytes, false);
    }

    /**
     * 下载链接内容
     *
     * @param url          链接
     * @param outputStream 输出流
     * @param connTimeout  连接超时时间
     * @param readTimeout  读取超时时间
     * @throws IOException 抛出IOException
     */
    public static void downloadUrlData(String url, OutputStream outputStream,
                                       int connTimeout, int readTimeout) throws IOException {
        downloadUrlData(url, outputStream, httpURLConnection -> {
            if (connTimeout > 0) {
                httpURLConnection.setConnectTimeout(connTimeout);
            }
            if (readTimeout > 0) {
                httpURLConnection.setReadTimeout(readTimeout);
            }
        });
    }

    /**
     * 下载链接内容
     *
     * @param url                链接
     * @param outputStream       输出流
     * @param connectionConsumer 连接处理函数
     * @throws IOException 抛出IOException
     */
    public static void downloadUrlData(String url, OutputStream outputStream,
                                       Consumer<HttpURLConnection> connectionConsumer) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        try {
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) "
                            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 "
                            + "Safari/537.36");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
            httpURLConnection.setRequestProperty("Referer", "no-referrer");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (connectionConsumer != null) {
                connectionConsumer.accept(httpURLConnection);
            }
            try (InputStream inputStream = httpURLConnection.getInputStream()) {
                byte[] buffer = new byte[1024 * 64];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * 下载链接内容(默认10秒连接超时、30秒读取超时)
     *
     * @param url          链接
     * @param outputStream 输出流
     * @throws IOException 抛出IOException
     */
    public static void downloadUrlData(String url, OutputStream outputStream) throws IOException {
        downloadUrlData(url, outputStream, 10000, 30000);
    }

    /**
     * 下载链接内容到字节数组(默认10秒连接超时、30秒读取超时)
     *
     * @param url 链接
     * @return 字节数组
     * @throws IOException 抛出IOException
     */
    public static byte[] downloadUrlData(String url) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            downloadUrlData(url, outputStream, 10000, 30000);
            return outputStream.toByteArray();
        }
    }

    /**
     * 下载链接内容到字节数组
     *
     * @param url         链接
     * @param connTimeout 连接超时时间
     * @param readTimeout 读取超时时间
     * @return 字节数组
     * @throws IOException 抛出IOException
     */
    public static byte[] downloadUrlData(String url, int connTimeout, int readTimeout) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            downloadUrlData(url, outputStream, connTimeout, readTimeout);
            return outputStream.toByteArray();
        }
    }

}
