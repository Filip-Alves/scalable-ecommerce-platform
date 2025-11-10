package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.model.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderSummaryResponse {
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
}