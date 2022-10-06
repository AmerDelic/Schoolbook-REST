package com.amerd.schoolbook.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Properties;

@Service
@Slf4j
@Validated
public class EmailService {
    private final String sender, host, username, password, smtpAuth, startTls;
    private final int port;
    private String recipient, text, subject;

    public EmailService(
            @Value("${spring.mail.username:}") String sender,
            @Value("${spring.mail.host:}") String host,
            @Value("${spring.mail.port:587}") String port,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${spring.mail.properties.mail.smtp.auth:}") String smtpAuth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:}") String startTls) {
        this.sender = sender;
        this.host = host;
        this.port = Integer.parseInt(port);
        this.username = username;
        this.password = password;
        this.smtpAuth = smtpAuth;
        this.startTls = startTls;
    }

    public String sendEmail() {
        if (emailContentsValid()) return sendSimpleMail();
        log.info("\n\n\t * WARNING * Failed to send new user email -- elements missing\n");
        return "Failed to send email";
    }

    private String sendSimpleMail() {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(sender);
            mailMessage.setTo(recipient);
            mailMessage.setText(text);
            mailMessage.setSubject(subject);
            JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
            javaMailSender.setHost(host);
            javaMailSender.setPort(port);
            javaMailSender.setUsername(username);
            javaMailSender.setPassword(password);
            Properties props = javaMailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.starttls.enable", startTls);
            props.put("mail.debug", "true");

            javaMailSender.send(mailMessage);
            this.recipient = null;
            this.text = null;
            this.subject = null;
            log.info(String.format("Sending mail -> %s", mailMessage));
            return "Mail Sent Successfully...";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Error while Sending Mail";
        }
    }

    public EmailService recipient(@NotNull String recipient) {
        this.recipient = recipient;
        return this;
    }

    public EmailService emailBody(@NotNull String text) {
        this.text = text;
        return this;
    }

    public EmailService subject(@NotNull String subject) {
        this.subject = subject;
        return this;
    }

    private boolean emailContentsValid() {
        return StringUtils.hasText(this.subject) &&
                StringUtils.hasText(this.text) &&
                StringUtils.hasText(this.recipient) &&
                StringUtils.hasText(this.sender);
    }
}
