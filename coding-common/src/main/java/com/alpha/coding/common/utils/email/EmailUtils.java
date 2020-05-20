package com.alpha.coding.common.utils.email;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * EmailUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class EmailUtils {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    private static final String DEFAULT_CHARSET_NAME = "UTF-8";

    public static boolean isEmailAddress(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 发送邮件
     *
     * @param sender  : 邮件发送器
     * @param from    : 发送方
     * @param tos     : 接收方
     * @param text    : 文案
     * @param subject : 标题
     * @param file    : 附件
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static void sendEmail(JavaMailSender sender, String from, String[] tos, String text, String subject,
                                 File file) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        if (file != null) {
            new EmailBuilder(message, true).from(from).to(tos).text(text).subject(subject)
                    .addAttachment(MimeUtility.encodeWord(file.getName(), DEFAULT_CHARSET_NAME,
                            null), file)
                    .send(sender);
        } else {
            new EmailBuilder(message).from(from).to(tos).text(text).subject(subject).send(sender);
        }
    }

    /**
     * 发送邮件
     *
     * @param sender             : 邮件发送器
     * @param from               : 发送方
     * @param tos                : 接收方
     * @param text               : 文案
     * @param subject            : 标题
     * @param attachmentFilename : 附件名
     * @param file               : 附件
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static void sendEmail(JavaMailSender sender, String from, String[] tos, String text, String subject,
                                 String attachmentFilename, File file)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = sender.createMimeMessage();
        if (file != null) {
            new EmailBuilder(message, true).from(from).to(tos).text(text).subject(subject)
                    .addAttachment(attachmentFilename, file).send(sender);
        } else {
            new EmailBuilder(message).from(from).to(tos).text(text).subject(subject).send(sender);
        }
    }

    /**
     * 发送邮件
     *
     * @param sender             : 邮件发送器
     * @param from               : 发送方
     * @param tos                : 接收方
     * @param text               : 文案
     * @param subject            : 标题
     * @param attachmentFilename : 附件名
     * @param iss                : 附件输入流
     *
     * @throws MessagingException
     */
    public static void sendEmail(JavaMailSender sender, String from, String[] tos, String text, String subject,
                                 String attachmentFilename, InputStreamSource iss) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        if (iss != null) {
            new EmailBuilder(message, true).from(from).to(tos).text(text).subject(subject)
                    .addAttachment(attachmentFilename, iss).send(sender);
        } else {
            new EmailBuilder(message).from(from).to(tos).text(text).subject(subject).send(sender);
        }
    }

    /**
     * 发送邮件
     * <p>
     * 注意拿到bytes的编码与sender的编码一致，否则可能出现乱码
     * </p>
     *
     * @param sender             : 邮件发送器
     * @param from               : 发送方
     * @param tos                : 接收方
     * @param text               : 文案
     * @param subject            : 标题
     * @param attachmentFilename : 附件名
     * @param bytes              : 附件字节
     *
     * @throws MessagingException
     */
    public static void sendEmail(JavaMailSender sender, String from, String[] tos, String text, String subject,
                                 String attachmentFilename, byte[] bytes) throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        if (bytes != null) {
            InputStreamSource iss = new ByteArrayResource(bytes);
            new EmailBuilder(message, true).from(from).to(tos).text(text).subject(subject)
                    .addAttachment(attachmentFilename, iss).send(sender);
        } else {
            new EmailBuilder(message).from(from).to(tos).text(text).subject(subject).send(sender);
        }
    }

    /**
     * 自己构造EmailBuilder，发送邮件
     *
     * @param sender  : 邮件发送器
     * @param builder : 邮件构造器
     */
    public static void sendEmail(JavaMailSender sender, EmailBuilder builder) {
        builder.send(sender);
    }

}
