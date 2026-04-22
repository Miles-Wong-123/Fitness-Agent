package com.miles.fitnessagent.auth;

import com.miles.fitnessagent.config.AppProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public boolean isEnabled() {
        return appProperties.getMail().isEnabled();
    }

    public void sendVerificationCode(String to, String code) {
        if (!isEnabled()) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        String from = appProperties.getMail().getFrom();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject("Fitness Agent verification code");
        message.setText("""
                Your Fitness Agent verification code is:

                %s

                It will expire soon. If you did not request this code, please ignore this email.
                """.formatted(code));
        mailSender.send(message);
    }
}
