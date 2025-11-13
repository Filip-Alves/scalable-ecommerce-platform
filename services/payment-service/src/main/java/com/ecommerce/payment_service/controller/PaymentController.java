package com.ecommerce.payment_service.controller;

import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.dto.ProcessPaymentRequest;
import com.ecommerce.payment_service.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{orderId}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.retryPayment(orderId);
        return ResponseEntity.ok(response);
    }
}