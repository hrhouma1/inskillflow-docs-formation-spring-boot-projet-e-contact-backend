package com.example.contact.service;

import com.example.contact.model.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Async
    public void sendNotificationToAdmin(Lead lead) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("Nouveau contact: " + lead.getFullName());
            message.setText(
                    "Nouveau contact reçu!\n\n" +
                    "Nom: " + lead.getFullName() + "\n" +
                    "Entreprise: " + (lead.getCompany() != null ? lead.getCompany() : "Non spécifié") + "\n" +
                    "Email: " + lead.getEmail() + "\n" +
                    "Téléphone: " + (lead.getPhone() != null ? lead.getPhone() : "Non spécifié") + "\n" +
                    "Type de demande: " + lead.getRequestType() + "\n\n" +
                    "Message:\n" + lead.getMessage() + "\n\n" +
                    "---\n" +
                    "Reçu le: " + lead.getCreatedAt()
            );

            mailSender.send(message);
            log.info("Email de notification envoyé à l'admin pour le lead: {}", lead.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à l'admin: {}", e.getMessage());
        }
    }

    @Async
    public void sendConfirmationToVisitor(Lead lead) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(lead.getEmail());
            message.setSubject("Confirmation - Nous avons bien reçu votre message");
            message.setText(
                    "Bonjour " + lead.getFullName() + ",\n\n" +
                    "Merci de nous avoir contactés!\n\n" +
                    "Nous avons bien reçu votre message concernant: " + lead.getRequestType() + "\n\n" +
                    "Notre équipe vous répondra dans les plus brefs délais.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe Support"
            );

            mailSender.send(message);
            log.info("Email de confirmation envoyé à: {}", lead.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation à {}: {}", lead.getEmail(), e.getMessage());
        }
    }
}

