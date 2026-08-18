package com.redmath.authentication.service;

import com.redmath.authentication.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Thymeleaf
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Async("mailExecutor")
    public void sendOtpEmail(String to, String name, String otp, long expiryDays) {

        String activationUrl = frontendBaseUrl + "/activate?email=" + URLEncoder.encode(to, StandardCharsets.UTF_8);

        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expiryDays", expiryDays);
        ctx.setVariable("activationUrl", activationUrl);
        String html = templateEngine.process("otp-email", ctx);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your one-time login code");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send OTP email", e);
        }
    }
}
