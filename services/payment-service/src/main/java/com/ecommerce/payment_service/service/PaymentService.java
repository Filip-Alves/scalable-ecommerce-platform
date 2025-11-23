package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.dto.ProcessPaymentRequest;
import com.ecommerce.payment_service.model.Payment;
import com.ecommerce.payment_service.model.PaymentStatus;
import com.ecommerce.payment_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Random random = new Random();
    private final MetricsService metricsService;

    public PaymentService(PaymentRepository paymentRepository, MetricsService metricsService) {
        this.paymentRepository = paymentRepository;
        this.metricsService = metricsService;
    }

    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        metricsService.incrementPaymentsProcessed();

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.PENDING);

        // Simule paiement (90% succès, 10% échec)
        boolean isSuccess = random.nextInt(100) < 90;

        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            metricsService.incrementPaymentsSuccess();
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            metricsService.incrementPaymentsFailed();
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(savedPayment.getId());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setStatus(savedPayment.getStatus());

        return response;
    }

    @Transactional
    public PaymentResponse retryPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment already successful");
        }

        // Retry simu
        boolean isSuccess = random.nextInt(100) < 90;

        if (isSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        payment.setTransactionId(UUID.randomUUID().toString());
        Payment savedPayment = paymentRepository.save(payment);

        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(savedPayment.getId());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setStatus(savedPayment.getStatus());

        return response;
    }
}