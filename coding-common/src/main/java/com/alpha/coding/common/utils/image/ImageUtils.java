package com.alpha.coding.common.utils.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import com.alpha.coding.common.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * ImageUtils
 *
 * @version 1.0
 * Date: 2020-01-14
 */
@Slf4j
public class ImageUtils {

    private static final double DEFAULT_PRECISION = 0.001d;
    private static final String FILE_NAME_REGEXP = "[？~`!@#$%^&*()=_+\\\\{}|;:,.<>?～•！￥…×（）—『』【】、；'：《》，。\"\\[\\]\\-]";
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEXP);

    /**
     * 获取图片类型
     *
     * @param imageBytes 图片bytes
     * @return 图片类型的名称
     * @throws IOException IOException
     */
    public static String getImageType(final byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInput);
            String type = null;
            if (iterator.hasNext()) {
                ImageReader reader = iterator.next();
                type = reader.getFormatName();
            }
            return type;
        }
    }

    /**
     * 获得图片基础信息。
     * 图片类型: bmp, jpeg, jpg, png
     * 图片宽
     * 图片高
     *
     * @param imageBytes 图片bytes
     * @return 图片元数据
     * @throws IOException IOException
     */
    public static ImageMeta getImageMeta(final byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
             ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInput);
            if (iterator.hasNext()) {
                ImageReader reader = iterator.next();
                reader.setInput(imageInput);
                return new ImageMeta()
                        .setType(reader.getFormatName().toLowerCase())
                        .setWidth(reader.getWidth(reader.getMinIndex()))
                        .setHeight(reader.getHeight(reader.getMinIndex()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将非JPEG图片转为JPEG
     *
     * @param imageBytes 图片bytes
     * @return 图片JPEG格式下bytes
     * @throws IOException IOException
     */
    public static byte[] img2Jpg(byte[] imageBytes, double quality) throws IOException, IllegalArgumentException {
        if (quality > 1.0d || quality < 0.0d) {
            throw new IllegalArgumentException("The param quality must be set between 0.0f and 1.0f");
        }
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage jpg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        jpg.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(Double.valueOf(quality).floatValue());
        writer.setOutput(ios);
        writer.write(null, new IIOImage(jpg, null, null), param);
        writer.dispose();
        return bos.toByteArray();
    }

    /**
     * 缩放图片
     *
     * @param data       原始图片数据
     * @param rectWidth  缩放后的宽度
     * @param rectHeight 缩放后的高度
     * @return 缩放图片数据
     * @throws IOException IOException
     */
    public static byte[] scale(byte[] data, int rectWidth, int rectHeight) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        BufferedImage scaled = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
        Image scaledIns = img.getScaledInstance(rectWidth, rectHeight, BufferedImage.SCALE_SMOOTH);
        scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1f);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(scaled, null, null), param);
        writer.dispose();
        return bos.toByteArray();
    }

    /**
     * 获取缩略图
     * 1. 根据短边比例等比例压缩
     * 2. 长边居中裁剪
     *
     * @param imageBytes 原始图片数据
     * @param destHeight 目标宽度
     * @param destWidth  目标高度
     * @return 缩略图数据
     * @throws IOException IOException
     */
    public static byte[] createSnapshot(byte[] imageBytes, int destWidth, int destHeight, Double precision)
            throws IOException {
        ImageWriter writer = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = null;
        try {
            writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            BufferedImage image = null;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
                createThumbnail(bais, baos, destWidth, destHeight,
                        precision == null ? DEFAULT_PRECISION : precision, "jpeg");
                try (ByteArrayInputStream newImgBais = new ByteArrayInputStream(baos.toByteArray())) {
                    image = ImageIO.read(newImgBais);
                }
            }
            ios = ImageIO.createImageOutputStream(bos);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1f);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            return bos.toByteArray();
        } finally {
            bos.close();
            if (writer != null) {
                writer.dispose();
            }
            if (ios != null) {
                ios.close();
            }
        }
    }

    /**
     * 校验除了svg外格式
     *
     * @param imageBytes 图片bytes
     * @return 是-当前支持的图片格式，否-非图片或非当前支持的图片格式
     * @throws IOException IOException
     */
    public static boolean isValidImageFormat(byte[] imageBytes) throws IOException {
        String format = getImageType(imageBytes);
        if (format == null) {
            return false;
        }
        for (ImageFormat value : ImageFormat.values()) {
            if (format.equalsIgnoreCase(value.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤图片名称特殊字符
     *
     * @param imageName 图片名
     * @return 过滤后的图片名
     */
    public static String filterImageName(String imageName) {
        Matcher m = FILE_NAME_PATTERN.matcher(imageName);
        return m.replaceAll("").trim();
    }

    /**
     * 图片透明背景
     *
     * @param inputStream  图片输入流
     * @param outputStream 图片输出流
     * @param alpha        alpha值
     * @param formatName   输出图片类型，默认为png
     */
    public static void transparentImage(InputStream inputStream, OutputStream outputStream,
                                        int alpha, String formatName) throws IOException {
        BufferedImage srcImage = ImageIO.read(inputStream);
        int imgHeight = srcImage.getHeight();
        int imgWidth = srcImage.getWidth();
        int c = srcImage.getRGB(3, 3);
        // 防止越位
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 10) {
            alpha = 10;
        }
        // 新建一个类型支持透明的BufferedImage
        BufferedImage bi = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_4BYTE_ABGR);
        // 把原图片的内容复制到新的图片，同时把背景设为透明
        for (int i = 0; i < imgWidth; ++i) {
            for (int j = 0; j < imgHeight; ++j) {
                //把背景设为透明
                if (srcImage.getRGB(i, j) == c) {
                    bi.setRGB(i, j, c & 0x00ffffff);
                }
                //设置透明度
                else {
                    int rgb = bi.getRGB(i, j);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    bi.setRGB(i, j, rgb);
                }
            }
        }
        writeImageAsBytes(bi, StringUtils.isBlank(formatName) ? "png" : formatName, outputStream);
    }

    /**
     * 标准化图片base64
     *
     * @param imageBase64 图片base64
     * @return 纯base64字符串
     */
    public static String normalizeImageBase64(String imageBase64) {
        final String marking = ";base64,";
        if (imageBase64.startsWith("data:image/") && imageBase64.contains(marking)) {
            return imageBase64.substring(imageBase64.indexOf(marking) + marking.length());
        }
        return imageBase64;
    }

    /**
     * 获取缩略图
     * 1. 根据短边比例等比例压缩
     * 2. 长边居中裁剪
     *
     * @param inputStream  图片输入流
     * @param outputStream 图片输出流
     * @param destWidth    目标宽度
     * @param destHeight   目标高度
     * @param precision    精度
     * @param formatName   输出格式，默认为png
     */
    public static void createThumbnail(InputStream inputStream, OutputStream outputStream,
                                       int destWidth, int destHeight, double precision,
                                       String formatName) throws IOException {
        BufferedImage src = ImageIO.read(inputStream);
        int srcHeight = src.getHeight();
        int srcWidth = src.getWidth();
        // 计算宽高比例
        double heightScalePercent = ((double) srcHeight) / destHeight;
        double widthScalePercent = ((double) srcWidth) / destWidth;
        if (Math.abs(widthScalePercent - heightScalePercent) < precision) {
            // 只进行缩放
            BufferedImage scaled = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
            Image scaledIns = src.getScaledInstance(destWidth, destHeight, BufferedImage.SCALE_SMOOTH);
            scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
            writeImageAsBytes(scaled, StringUtils.isBlank(formatName) ? "png" : formatName, outputStream);
        } else if (widthScalePercent < heightScalePercent) {
            // 宽比小，按照宽比缩放，然后居中裁剪高
            int scaleHeight = (int) (srcHeight / widthScalePercent);
            BufferedImage scaled = new BufferedImage(destWidth, scaleHeight, BufferedImage.TYPE_INT_RGB);
            Image scaledIns = src.getScaledInstance(destWidth, scaleHeight, BufferedImage.SCALE_SMOOTH);
            scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
            // 裁剪
            int y = (scaleHeight - destHeight) / 2;
            BufferedImage retImage = scaled.getSubimage(0, y, destWidth, destHeight);
            writeImageAsBytes(retImage, StringUtils.isBlank(formatName) ? "png" : formatName, outputStream);
        } else {
            // 高比小，按照高比缩放，然后居中裁剪宽
            int scaleWidth = (int) (srcWidth / heightScalePercent);
            BufferedImage scaled = new BufferedImage(scaleWidth, destHeight, BufferedImage.TYPE_INT_RGB);
            Image scaledIns = src.getScaledInstance(scaleWidth, destHeight, BufferedImage.SCALE_SMOOTH);
            scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
            // 裁剪
            int x = (scaleWidth - destWidth) / 2;
            BufferedImage retImage = scaled.getSubimage(x, 0, destWidth, destHeight);
            writeImageAsBytes(retImage, StringUtils.isBlank(formatName) ? "png" : formatName, outputStream);
        }
    }

    /**
     * 将宽度相同的图片，竖向追加在一起，默认输出为jpeg格式
     * <p>注意：宽度必须相同</p>
     * <p>图片较多时容易占用较多内存导致溢出</p>
     *
     * @param imageList    文件流数组
     * @param outputStream 输出流
     * @param formatName   输出图片格式，默认为jpeg
     */
    @Deprecated
    public static void spliceImages(List<BufferedImage> imageList, OutputStream outputStream, String formatName)
            throws IOException {
        if (imageList == null || imageList.size() <= 0) {
            return;
        }
        int height = 0; // 总高度
        int width = 0; // 总宽度
        int tmpHeight = 0; // 临时的高度 , 或保存偏移高度
        int tmpEveryHeight = 0; // 临时的高度，主要保存每个高度
        int picNum = imageList.size();// 图片的数量
        int[] heightArray = new int[picNum]; // 保存每个文件的高度
        BufferedImage buffer = null; // 保存图片流
        List<int[]> imgRGB = new ArrayList<>(picNum); // 保存所有的图片的RGB
        int[] imgRGBs; // 保存一张图片中的RGB数据
        for (int i = 0; i < picNum; i++) {
            buffer = imageList.get(i);
            heightArray[i] = tmpHeight = buffer.getHeight();// 图片高度
            if (i == 0) {
                width = buffer.getWidth();// 图片宽度
            }
            height += tmpHeight; // 获取总高度
            imgRGBs = new int[width * tmpHeight];// 从图片中读取RGB
            imgRGBs = buffer.getRGB(0, 0, width, tmpHeight, imgRGBs, 0, width);
            imgRGB.add(imgRGBs);
        }
        tmpHeight = 0; // 设置偏移高度为0
        // 生成新图片
        BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < picNum; i++) {
            tmpEveryHeight = heightArray[i];
            if (i != 0) {
                tmpHeight += (tmpEveryHeight); // 计算偏移高度
            }
            imageResult.setRGB(0, tmpHeight, width, tmpEveryHeight, imgRGB.get(i), 0, width); // 写入流中
        }
        writeImageAsBytes(imageResult, StringUtils.isBlank(formatName) ? "jpeg" : formatName, outputStream); // 写图片
    }

    /**
     * 将宽度相同的图片，竖向追加在一起，默认输出为jpeg格式
     * <p>注意：宽度必须相同</p>
     *
     * @param imageList    文件流数组
     * @param outputStream 输出流
     */
    public static void spliceImages(List<BufferedImage> imageList, OutputStream outputStream) throws IOException {
        spliceImages(imageList, outputStream, "jpeg");
    }

    /**
     * 将{@link BufferedImage}生成formatName指定格式的图像数据
     *
     * @param source       图片源
     * @param formatName   图像格式名，图像格式名错误则抛出异常
     * @param outputStream 输出流
     */
    public static void writeImageAsBytes(BufferedImage source, String formatName, OutputStream outputStream)
            throws IOException {
        Graphics2D graphics2D = null;
        try {
            for (BufferedImage targetSrc = source; !ImageIO.write(targetSrc, formatName, outputStream); ) {
                if (graphics2D != null) {
                    throw new IllegalArgumentException(String.format("not found writer for '%s'", formatName));
                }
                targetSrc = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                graphics2D = targetSrc.createGraphics();
                graphics2D.drawImage(source, 0, 0, null);
            }
        } finally {
            if (graphics2D != null) {
                graphics2D.dispose();
            }
        }
    }

    /**
     * 将{@link BufferedImage}生成formatName指定格式的图像数据
     *
     * @param source     图片源
     * @param formatName 图像格式名，图像格式名错误则抛出异常
     */
    public static byte[] writeImageAsBytes(BufferedImage source, String formatName) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            writeImageAsBytes(source, formatName, output);
            return output.toByteArray();
        }
    }

}
