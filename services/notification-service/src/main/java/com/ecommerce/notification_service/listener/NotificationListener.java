package com.ecommerce.notification_service.listener;

import com.ecommerce.notification_service.event.OrderNotificationEvent;
import com.ecommerce.notification_service.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final EmailService emailService;

    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "order-notifications")
    public void handleOrderNotification(OrderNotificationEvent event) {
        System.out.println("Event reçu : " + event.getEventType() + " pour commande #" + event.getOrderId());
        emailService.sendOrderNotification(event);
    }
}