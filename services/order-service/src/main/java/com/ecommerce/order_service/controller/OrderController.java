package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.dto.OrderSummaryResponse;
import com.ecommerce.order_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@RequestHeader("X-User-Id") Long userId) {
        OrderResponse response = orderService.checkout(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getUserOrders(
            @RequestHeader("X-User-Id") Long userId) {
        List<OrderSummaryResponse> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        OrderResponse order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }
}