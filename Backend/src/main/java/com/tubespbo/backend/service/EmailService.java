package com.tubespbo.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(
            String recipientEmail,
            String verificationLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipientEmail);
        message.setSubject("Verifikasi Akun");

        message.setText(
                "Halo!\n\n" +
                "Klik link berikut untuk verifikasi akun:\n\n" +
                verificationLink +
                "\n\nLink berlaku 15 menit."
        );

        mailSender.send(message);
    }
}