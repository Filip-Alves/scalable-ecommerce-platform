package com.ecommerce.order_service.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter ordersCreatedCounter;
    private final Counter paymentsSuccessCounter;
    private final Counter paymentsFailedCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created")
                .register(meterRegistry);

        this.paymentsSuccessCounter = Counter.builder("payments.success.total")
                .description("Total number of successful payments")
                .register(meterRegistry);

        this.paymentsFailedCounter = Counter.builder("payments.failed.total")
                .description("Total number of failed payments")
                .register(meterRegistry);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementPaymentsSuccess() {
        paymentsSuccessCounter.increment();
    }

    public void incrementPaymentsFailed() {
        paymentsFailedCounter.increment();
    }
}