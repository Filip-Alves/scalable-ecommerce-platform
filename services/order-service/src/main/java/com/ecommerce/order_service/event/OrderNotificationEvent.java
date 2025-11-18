package com.ecommerce.order_service.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderNotificationEvent {
    private String eventType;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
}