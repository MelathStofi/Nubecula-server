package com.melath.nubecula.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailSenderService {


    private final JavaMailSender javaMailSender;

    @Value("${base.url}")
    private String baseUrl;

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    void sendEmail(String email, String username) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);

        msg.setSubject("Registration");
        msg.setText("Welcome " + username + "!\n\n" +
                "Click on this link and you're done!\n\n" + baseUrl + "/auth/sign-in\n\n" +
                "Cheers!\n\nNubecula");
        try {
            javaMailSender.send(msg);
            log.info("E-mail was sent to: " + email + "!");

        } catch (MailException e) {
            log.error("E-mail couldn't be sent to: " + email + "!\n" + e.getMessage());
        }
    }
}
