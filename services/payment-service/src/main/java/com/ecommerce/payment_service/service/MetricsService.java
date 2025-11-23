package com.ecommerce.payment_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter paymentsProcessedCounter;
    private final Counter paymentsSuccessCounter;
    private final Counter paymentsFailedCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.paymentsProcessedCounter = Counter.builder("payments.processed.total")
                .description("Total number of payments processed")
                .register(meterRegistry);

        this.paymentsSuccessCounter = Counter.builder("payments.success.total")
                .description("Total number of successful payments")
                .register(meterRegistry);

        this.paymentsFailedCounter = Counter.builder("payments.failed.total")
                .description("Total number of failed payments")
                .register(meterRegistry);
    }

    public void incrementPaymentsProcessed() {
        paymentsProcessedCounter.increment();
    }

    public void incrementPaymentsSuccess() {
        paymentsSuccessCounter.increment();
    }

    public void incrementPaymentsFailed() {
        paymentsFailedCounter.increment();
    }
}