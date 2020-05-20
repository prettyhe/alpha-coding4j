package com.alpha.coding.common.utils.email;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * EmailBuilder
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class EmailBuilder {

    private MimeMessageHelper messageHelper;

    public EmailBuilder(MimeMessage mimeMessage) {
        this(mimeMessage, null);
    }

    public EmailBuilder(MimeMessage mimeMessage, String encoding) {
        messageHelper = new MimeMessageHelper(mimeMessage, encoding);
    }

    public EmailBuilder(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
        this(mimeMessage, multipart, null);
    }

    public EmailBuilder(MimeMessage mimeMessage, boolean multipart, String encoding) throws MessagingException {
        messageHelper = new MimeMessageHelper(mimeMessage, multipart, encoding);
    }

    public EmailBuilder(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        this(mimeMessage, multipartMode, null);
    }

    public EmailBuilder(MimeMessage mimeMessage, int multipartMode, String encoding) throws MessagingException {
        messageHelper = new MimeMessageHelper(mimeMessage, multipartMode, encoding);
    }

    // start of sender
    public void send(JavaMailSender sender) {
        sender.send(messageHelper.getMimeMessage());
    }

    public EmailBuilder from(InternetAddress from) throws MessagingException {
        messageHelper.setFrom(from);
        return this;
    }

    public EmailBuilder from(String from) throws MessagingException {
        messageHelper.setFrom(from);
        return this;
    }

    public EmailBuilder from(String from, String personal) throws UnsupportedEncodingException, MessagingException {
        messageHelper.setFrom(from, personal);
        return this;
    }

    public EmailBuilder replyTo(InternetAddress replyTo) throws MessagingException {
        messageHelper.setReplyTo(replyTo);
        return this;
    }

    public EmailBuilder replyTo(String replyTo) throws MessagingException {
        messageHelper.setReplyTo(replyTo);
        return this;
    }

    public EmailBuilder replyTo(String replyTo, String personal) throws UnsupportedEncodingException,
            MessagingException {
        messageHelper.setReplyTo(replyTo, personal);
        return this;
    }

    public EmailBuilder to(InternetAddress to) throws MessagingException {
        messageHelper.setTo(to);
        return this;
    }

    public EmailBuilder to(InternetAddress[] to) throws MessagingException {
        messageHelper.setTo(to);
        return this;
    }

    public EmailBuilder to(String to) throws MessagingException {
        messageHelper.setTo(to);
        return this;
    }

    public EmailBuilder to(String[] to) throws MessagingException {
        messageHelper.setTo(to);
        return this;
    }

    public EmailBuilder addTo(InternetAddress to) throws MessagingException {
        messageHelper.addTo(to);
        return this;
    }

    public EmailBuilder addTo(String to) throws MessagingException {
        messageHelper.addTo(to);
        return this;
    }

    public EmailBuilder addTo(String to, String personal) throws UnsupportedEncodingException, MessagingException {
        messageHelper.addTo(to, personal);
        return this;
    }

    public EmailBuilder cc(InternetAddress cc) throws MessagingException {
        messageHelper.setCc(cc);
        return this;
    }

    public EmailBuilder cc(InternetAddress[] cc) throws MessagingException {
        messageHelper.setCc(cc);
        return this;
    }

    public EmailBuilder cc(String cc) throws MessagingException {
        messageHelper.setCc(cc);
        return this;
    }

    public EmailBuilder cc(String[] cc) throws MessagingException {
        messageHelper.setCc(cc);
        return this;
    }

    public EmailBuilder addCc(InternetAddress cc) throws MessagingException {
        messageHelper.addCc(cc);
        return this;
    }

    public EmailBuilder addCc(String cc) throws MessagingException {
        messageHelper.addCc(cc);
        return this;
    }

    public EmailBuilder addCc(String cc, String personal) throws UnsupportedEncodingException, MessagingException {
        messageHelper.addCc(cc, personal);
        return this;
    }

    public EmailBuilder bcc(InternetAddress bcc) throws MessagingException {
        messageHelper.setBcc(bcc);
        return this;
    }

    public EmailBuilder bcc(InternetAddress[] bcc) throws MessagingException {
        messageHelper.setBcc(bcc);
        return this;
    }

    public EmailBuilder bcc(String bcc) throws MessagingException {
        messageHelper.setBcc(bcc);
        return this;
    }

    public EmailBuilder bcc(String[] bcc) throws MessagingException {
        messageHelper.setBcc(bcc);
        return this;
    }

    public EmailBuilder addBcc(InternetAddress bcc) throws MessagingException {
        messageHelper.addBcc(bcc);
        return this;
    }

    public EmailBuilder addBcc(String bcc) throws MessagingException {
        messageHelper.addBcc(bcc);
        return this;
    }

    public EmailBuilder addBcc(String bcc, String personal) throws UnsupportedEncodingException, MessagingException {
        messageHelper.addBcc(bcc, personal);
        return this;
    }

    public EmailBuilder sendDate(Date sendDate) throws MessagingException {
        messageHelper.setSentDate(sendDate);
        return this;
    }

    public EmailBuilder subject(String subject) throws MessagingException {
        messageHelper.setSubject(subject);
        return this;
    }

    public EmailBuilder text(String text) throws MessagingException {
        messageHelper.setText(text);
        return this;
    }

    public EmailBuilder text(String text, boolean html) throws MessagingException {
        messageHelper.setText(text, html);
        return this;
    }

    public EmailBuilder text(String plainText, String htmlText) throws MessagingException {
        messageHelper.setText(plainText, htmlText);
        return this;
    }

    public EmailBuilder addInline(String contentId, DataSource dataSource) throws MessagingException {
        messageHelper.addInline(contentId, dataSource);
        return this;
    }

    public EmailBuilder addInline(String contentId, File file) throws MessagingException {
        messageHelper.addInline(contentId, file);
        return this;
    }

    public EmailBuilder addInline(String contentId, Resource resource) throws MessagingException {
        messageHelper.addInline(contentId, resource);
        return this;
    }

    public EmailBuilder addInline(String contentId, InputStreamSource inputStreamSource, String contentType)
            throws MessagingException {
        messageHelper.addInline(contentId, inputStreamSource, contentType);
        return this;
    }

    public EmailBuilder addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
        messageHelper.addAttachment(attachmentFilename, dataSource);
        return this;
    }

    public EmailBuilder addAttachment(String attachmentFilename, File file) throws MessagingException {
        messageHelper.addAttachment(attachmentFilename, file);
        return this;
    }

    public EmailBuilder addAttachment(String attachmentFilename, InputStreamSource inputStreamSource)
            throws MessagingException {
        messageHelper.addAttachment(attachmentFilename, inputStreamSource);
        return this;
    }

    public EmailBuilder addAttachment(String attachmentFilename, InputStreamSource inputStreamSource,
                                      String contentType) throws MessagingException {
        messageHelper.addAttachment(attachmentFilename, inputStreamSource, contentType);
        return this;
    }
}
