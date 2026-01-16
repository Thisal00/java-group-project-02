package com.evmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for handling all SMTP email communication, especially for OTP delivery.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Note: The sending email address is configured in application.properties
    // private String fromEmail = "harvestmaster69@gmail.com";

    /**
     * Sends a simple text email with a subject and body.
     * @param to The recipient's email address.
     * @param subject The subject line of the email.
     * @param body The main content of the email.
     */
    public boolean sendSimpleMail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            // mail.setFrom(fromEmail); // Can be automatically determined from config

            javaMailSender.send(mail);
            return true;
        } catch (MailException e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            // In a real application, you would log this error.
            return false;
        }
    }
}