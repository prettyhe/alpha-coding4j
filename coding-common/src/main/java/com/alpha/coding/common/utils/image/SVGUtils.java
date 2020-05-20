package com.alpha.coding.common.utils.image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import lombok.extern.slf4j.Slf4j;

/**
 * SVGUtils
 *
 * @version 1.0
 * Date: 2020-01-14
 */
@Slf4j
public class SVGUtils {

    /**
     * 验证文件内容是否是svg文件。目前的方法是用第三方库尝试转svg到png，如果结果正常，则确定文件是svg文件，如果转换结果不正常，则不是svg格式文件
     *
     * @param imageByte 图片bytes
     *
     * @return 是svg就返回true，否则false
     */
    public static boolean validateSVG(final byte[] imageByte) {
        if (imageByte == null || imageByte.length == 0) {
            return false;
        }
        InputStream inputStream = new ByteArrayInputStream(imageByte);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        byte[] resultByte = null;

        Transcoder tr = new PNGTranscoder();

        TranscoderInput input = new TranscoderInput(inputStream);
        TranscoderOutput output = new TranscoderOutput(byteOutputStream);

        try {
            tr.transcode(input, output); // 转化svg到png
            resultByte = byteOutputStream.toByteArray();

            if (resultByte == null || resultByte.length == 0) {
                log.error("afterFilter transfer svg to png, got empty bytes! return false");
                return false;
            }
            // 确认转化后是否是png格式
            String type = ImageUtils.getImageType(resultByte);

            if (type != null && type.equalsIgnoreCase(ImageFormat.PNG.name())) {
                return true;
            } else {
                log.error("transfer result is not png type :: {}", type);
                return false;
            }
        } catch (UTFDataFormatException ue) { // 转化过程异常
            log.error("UTFDataFormatException when transcode, message :: {}", ue);
            return false;
        } catch (TranscoderException te) { // 转化过程异常
            log.error("TranscoderException when transcode, message :: {}", te);
            return false;
        } catch (IOException ie) { // 获取转化后格式类型异常
            log.error("IOException when get Image Type, message :: {}", ie);
            return false;
        } finally {
            try {
                inputStream.close();
                byteOutputStream.close();
            } catch (Exception e) {
                log.error("error when closing inputstream and outputstream");
            }
        }
    }

}
