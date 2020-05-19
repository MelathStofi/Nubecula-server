package com.melath.nubecula.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailSenderService {


    private JavaMailSender javaMailSender;

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    void sendEmail(String email, String username) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);

        msg.setSubject("Resister " + username + "!");
        msg.setText("You are now registered");

        try {
            javaMailSender.send(msg);
            log.info("E-mail was sent to: " + email + "!");

        } catch (MailException e) {
            log.error("E-mail couldn't be sent to: " + email + "!\n" + e.getMessage());
        }
    }
}
