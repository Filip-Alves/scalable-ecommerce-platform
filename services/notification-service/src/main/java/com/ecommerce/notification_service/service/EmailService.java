package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.event.OrderNotificationEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderNotification(OrderNotificationEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@ecommerce.com");
        message.setTo(event.getUserEmail());

        switch (event.getEventType()) {
            case "PAYMENT_SUCCESS":
                message.setSubject("Commande confirmée #" + event.getOrderId());
                message.setText(
                        "Bonjour,\n\n" +
                                "Votre commande #" + event.getOrderId() + " a été confirmée et payée.\n" +
                                "Montant total : " + event.getTotalAmount() + " €\n\n" +
                                "Merci pour votre achat !\n\n" +
                                "L'équipe E-Commerce"
                );
                break;

            case "PAYMENT_FAILED":
                message.setSubject("Échec du paiement - Commande #" + event.getOrderId());
                message.setText(
                        "Bonjour,\n\n" +
                                "Le paiement de votre commande #" + event.getOrderId() + " a échoué.\n" +
                                "Montant : " + event.getTotalAmount() + " €\n\n" +
                                "Veuillez réessayer ou contacter notre support.\n\n" +
                                "L'équipe E-Commerce"
                );
                break;

            default:
                return;
        }

        mailSender.send(message);
        System.out.println("Email envoyé à " + event.getUserEmail() + " pour l'event " + event.getEventType());
    }
}