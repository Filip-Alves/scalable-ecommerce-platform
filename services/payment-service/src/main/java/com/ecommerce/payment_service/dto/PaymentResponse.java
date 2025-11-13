package com.ecommerce.payment_service.dto;

import com.ecommerce.payment_service.model.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String transactionId;
    private PaymentStatus status;
}