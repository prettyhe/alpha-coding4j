package com.alpha.coding.common.utils.image;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

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

    /**
     * 获取图片类型
     *
     * @param imageBytes 图片bytes
     *
     * @return 图片类型的名称
     *
     * @throws IOException
     */
    public static String getImageType(final byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        ImageInputStream imageInput = ImageIO.createImageInputStream(input);
        Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInput);
        String type = null;
        if (iterator.hasNext()) {
            ImageReader reader = iterator.next();
            type = reader.getFormatName();
        }

        try {
            return type;
        } finally {
            if (imageInput != null) {
                imageInput.close();
            }
        }
    }

    /**
     * 获得图片基础信息。
     * 图片类型: bmp, jpeg, jpg, png
     * 图片宽
     * 图片高
     *
     * @param imageBytes 图片bytes
     *
     * @return 图片元数据
     *
     * @throws IOException
     */
    public static ImageMeta getImageMeta(final byte[] imageBytes) throws IOException {
        ByteArrayInputStream input = null;
        ImageInputStream imageInput = null;

        try {
            input = new ByteArrayInputStream(imageBytes);
            imageInput = ImageIO.createImageInputStream(input);
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInput);
            if (iterator.hasNext()) {
                ImageReader reader = iterator.next();
                reader.setInput(imageInput);
                ImageMeta meta = new ImageMeta()
                        .setType(reader.getFormatName().toLowerCase())
                        .setWidth(reader.getWidth(reader.getMinIndex()))
                        .setHeight(reader.getHeight(reader.getMinIndex()));
                return meta;
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            if (input != null) {
                input.close();
            }
            if (imageInput != null) {
                imageInput.close();
            }
        }
    }

    /**
     * 将非JPEG图片转为JPEG
     *
     * @param imageBytes 图片bytes
     *
     * @return 图片JPEG格式下bytes
     *
     * @throws IOException
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
     *
     * @return 缩放图片数据
     *
     * @throws IOException
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
     *
     * @return 缩略图数据
     *
     * @throws IOException
     */
    public static byte[] createSnapshot(byte[] imageBytes, int destWidth, int destHeight, Double precision)
            throws IOException {
        ImageWriter writer = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = null;

        try {
            writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            BufferedImage image =
                    snapshot(imageBytes, destWidth, destHeight, precision == null ? DEFAULT_PRECISION : precision);
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
     * 获取缩略图
     * 1. 根据短边比例等比例压缩
     * 2. 长边居中裁剪
     *
     * @param imageBytes 原始图片数据
     * @param destWidth  目标宽度
     * @param destHeight 目标高度
     *
     * @return 缩略图数据
     *
     * @throws IOException
     */
    private static BufferedImage snapshot(byte[] imageBytes, int destWidth, int destHeight, double precision)
            throws IOException {
        BufferedImage src = ImageIO.read(new ByteArrayInputStream(imageBytes));
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
            return scaled;
        } else if (widthScalePercent < heightScalePercent) {
            // 宽比小，按照宽比缩放，然后居中裁剪高
            int scaleHeight = (int) (srcHeight / widthScalePercent);
            BufferedImage scaled = new BufferedImage(destWidth, scaleHeight, BufferedImage.TYPE_INT_RGB);
            Image scaledIns = src.getScaledInstance(destWidth, scaleHeight, BufferedImage.SCALE_SMOOTH);
            scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
            // 裁剪
            int y = (scaleHeight - destHeight) / 2;
            BufferedImage retImage = scaled.getSubimage(0, y, destWidth, destHeight);
            return retImage;
        } else {
            // 高比小，按照高比缩放，然后居中裁剪宽
            int scaleWidth = (int) (srcWidth / heightScalePercent);
            BufferedImage scaled = new BufferedImage(scaleWidth, destHeight, BufferedImage.TYPE_INT_RGB);
            Image scaledIns = src.getScaledInstance(scaleWidth, destHeight, BufferedImage.SCALE_SMOOTH);
            scaled.createGraphics().drawImage(scaledIns, 0, 0, Color.WHITE, null);
            // 裁剪
            int x = (scaleWidth - destWidth) / 2;
            BufferedImage retImage = scaled.getSubimage(x, 0, destWidth, destHeight);
            return retImage;
        }
    }

    /**
     * 校验除了svg外格式
     *
     * @param imageBytes 图片bytes
     *
     * @return 是-当前支持的图片格式，否-非图片或非当前支持的图片格式
     *
     * @throws IOException
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
     *
     * @return 过滤后的图片名
     *
     * @throws PatternSyntaxException
     */
    public static String filterImageName(String imageName) throws PatternSyntaxException {
        String regEx = "[？~`!@#$%^&*()=_+\\\\{}|;:,.<>?～•！@#￥%……&×（）—『』【】、；'：《》，。\\'\"\\[\\]\\-]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(imageName);
        return m.replaceAll("").trim();
    }

}
