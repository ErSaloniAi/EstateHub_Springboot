package com.estatehub.service;

import com.estatehub.entity.Customer;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * EmailService - migrated from GmailSender.java
 *
 * Preserves ALL email logic from GmailSender.gmail():
 *   sender = "estatehub807@gmail.com"
 *   receiver = RealEstate.emailID
 *   Subject: "Property Details from ESTATE HUB"
 *   Body: "Dear User,\n\nFind attached the property details you purchased.\n\nRegards,\nESTATE HUB"
 *   Attachment: FileMerger.mergeFiles(Customer.cityName) → property_{cityName}.txt
 *
 * Preserves SendFailedException handling from GmailSender:
 *   getInvalidAddresses() → log invalid addresses
 *   getValidUnsentAddresses() → log valid but unsent addresses
 *
 * Also adds OTP email, welcome email, password reset email for web auth.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // From GmailSender.java: sender = "estatehub807@gmail.com"
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Migrated from GmailSender.gmail()
     * Sends property purchase confirmation email with attachment.
     *
     * Original:
     *   msg.setSubject("Property Details from ESTATE HUB");
     *   textPart.setText("Dear User,\n\nFind attached the property details...\n\nRegards,\nESTATE HUB");
     *   attachmentPart.attachFile(attachment);
     */
    @Async
    public void sendPropertyPurchaseEmail(String receiverEmail, File attachmentFile) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);

            // From GmailSender.java
            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("Property Details from ESTATE HUB");
            helper.setText("Dear User,\n\nFind attached the property details you purchased.\n\nRegards,\nESTATE HUB");

            // Attachment — migrated from attachmentPart.attachFile(attachment)
            if (attachmentFile != null && attachmentFile.exists()) {
                FileSystemResource file = new FileSystemResource(attachmentFile);
                helper.addAttachment(attachmentFile.getName(), file);
            }

            mailSender.send(msg);
            log.info("Email sent successfully with attachment to: {}", receiverEmail);

        } catch (MessagingException e) {
            // Preserves GmailSender SendFailedException handling
            log.error("Email sending failed to: {}", receiverEmail);
            if (e instanceof SendFailedException sfe) {
                if (sfe.getInvalidAddresses() != null) {
                    log.error("Invalid Email Address(es):");
                    for (Address a : sfe.getInvalidAddresses()) {
                        log.error("  {}", a.toString());
                    }
                }
                if (sfe.getValidUnsentAddresses() != null) {
                    log.warn("Valid but UN-SENT Email Addresses:");
                    for (Address a : sfe.getValidUnsentAddresses()) {
                        log.warn("  {}", a.toString());
                    }
                }
            }
            log.error("Messaging exception: {}", e.getMessage());
        } catch (Exception e) {
            log.error("General exception in email: {}", e.getMessage());
        }
    }

    /**
     * Sends OTP verification email for web registration.
     */
    @Async
    public void sendOtpEmail(String receiverEmail, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("ESTATE HUB - Email Verification OTP");
            helper.setText(
                "<html><body style='font-family:Arial,sans-serif;'>"
                + "<div style='max-width:500px;margin:0 auto;padding:30px;border:1px solid #e0e0e0;border-radius:10px;'>"
                + "<h2 style='color:#1a1a2e;'>ESTATE HUB</h2>"
                + "<p>Your OTP for email verification is:</p>"
                + "<div style='font-size:36px;font-weight:bold;color:#c9a84c;letter-spacing:8px;padding:20px;text-align:center;background:#f5f5f5;border-radius:8px;'>"
                + otp
                + "</div>"
                + "<p>This OTP is valid for <strong>10 minutes</strong>.</p>"
                + "<p style='color:#666;font-size:12px;'>If you didn't request this, please ignore this email.</p>"
                + "<hr/><p style='color:#999;font-size:11px;'>Regards,<br/><strong>ESTATE HUB</strong></p>"
                + "</div></body></html>",
                true
            );

            mailSender.send(msg);
            log.info("OTP email sent to: {}", receiverEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage());
        }
    }

    /**
     * Sends welcome email after successful registration.
     */
    @Async
    public void sendWelcomeEmail(String receiverEmail, String customerName) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("Welcome to ESTATE HUB!");
            helper.setText(
                "<html><body style='font-family:Arial,sans-serif;'>"
                + "<div style='max-width:500px;margin:0 auto;padding:30px;border:1px solid #e0e0e0;border-radius:10px;'>"
                + "<h2 style='color:#1a1a2e;'>Welcome to ESTATE HUB, " + customerName + "!</h2>"
                + "<p>Your account has been successfully created.</p>"
                + "<p>You can now browse properties, connect as a buyer or seller, and manage your real estate journey with us.</p>"
                + "<hr/><p>Regards,<br/><strong>ESTATE HUB Team</strong></p>"
                + "</div></body></html>",
                true
            );

            mailSender.send(msg);
            log.info("Welcome email sent to: {}", receiverEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    /**
     * Sends password reset email with token link.
     */
    @Async
    public void sendPasswordResetEmail(String receiverEmail, String resetLink) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("ESTATE HUB - Password Reset Request");
            helper.setText(
                "<html><body style='font-family:Arial,sans-serif;'>"
                + "<div style='max-width:500px;margin:0 auto;padding:30px;border:1px solid #e0e0e0;border-radius:10px;'>"
                + "<h2 style='color:#1a1a2e;'>ESTATE HUB - Password Reset</h2>"
                + "<p>You requested a password reset. Click the link below to reset your password:</p>"
                + "<a href='" + resetLink + "' style='display:inline-block;padding:12px 24px;background:#c9a84c;color:#fff;text-decoration:none;border-radius:6px;font-weight:bold;'>Reset Password</a>"
                + "<p style='color:#666;font-size:12px;margin-top:20px;'>This link is valid for 1 hour. If you didn't request this, please ignore.</p>"
                + "<hr/><p>Regards,<br/><strong>ESTATE HUB Team</strong></p>"
                + "</div></body></html>",
                true
            );

            mailSender.send(msg);
            log.info("Password reset email sent to: {}", receiverEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }

    /**
     * Sends loan confirmation email — migrated from Loan.createLoan() file writing logic
     * Original wrote to "transaction{cityName}.txt"; this sends it via email.
     */
    @Async
    public void sendLoanConfirmationEmail(String receiverEmail, String bankName,
                                           double loanAmount, double emiAmount,
                                           int tenureMonths) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(receiverEmail);
            helper.setSubject("ESTATE HUB - Bank Loan Details");
            helper.setText(
                "<html><body style='font-family:Arial,sans-serif;'>"
                + "<div style='max-width:500px;margin:0 auto;padding:30px;border:1px solid #e0e0e0;border-radius:10px;'>"
                + "<h2 style='color:#1a1a2e;'>Bank Loan Details</h2>"
                // Mirrors loan file content from Loan.createLoan():
                // "================ Bank Loan Details ================"
                // "Bank: " + bankName + " | Loan Amount: " + loanAmount + " | EMI: " + emiAmount
                + "<table style='width:100%;border-collapse:collapse;'>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #eee;'><strong>Bank</strong></td><td style='padding:8px;border-bottom:1px solid #eee;'>" + bankName + "</td></tr>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #eee;'><strong>Loan Amount</strong></td><td style='padding:8px;border-bottom:1px solid #eee;'>₹" + String.format("%.2f", loanAmount) + "</td></tr>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #eee;'><strong>Total (with 1.5% brokerage)</strong></td><td style='padding:8px;border-bottom:1px solid #eee;'>₹" + String.format("%.2f", loanAmount * 1.015) + "</td></tr>"
                + "<tr><td style='padding:8px;border-bottom:1px solid #eee;'><strong>Tenure</strong></td><td style='padding:8px;border-bottom:1px solid #eee;'>" + tenureMonths + " months</td></tr>"
                + "<tr><td style='padding:8px;'><strong>EMI Amount</strong></td><td style='padding:8px;color:#c9a84c;font-weight:bold;'>₹" + String.format("%.2f", emiAmount) + "/month</td></tr>"
                + "</table>"
                + "<hr/><p>Regards,<br/><strong>ESTATE HUB Team</strong></p>"
                + "</div></body></html>",
                true
            );

            mailSender.send(msg);
            log.info("Loan confirmation email sent to: {}", receiverEmail);

        } catch (Exception e) {
            log.error("Failed to send loan email: {}", e.getMessage());
        }
    }
}
