package com.ecommerce.order_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String transactionId;
    private String status;
}