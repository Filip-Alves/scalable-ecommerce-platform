package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderItem;
import com.ecommerce.order_service.model.OrderStatus;
import com.ecommerce.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public OrderService(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder;
    }

    @Transactional
    public OrderResponse checkout(Long userId) {

        List<CartItemDto> cartItems = webClientBuilder.build()
                .get()
                .uri("http://shopping-cart-service/api/cart")
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToFlux(CartItemDto.class)
                .collectList()
                .block();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        //Check stock
        for (CartItemDto item : cartItems) {
            StockResponse stock = webClientBuilder.build()
                    .get()
                    .uri("http://product-catalog-service/api/products/{id}/stock", item.getProductId())
                    .retrieve()
                    .bodyToMono(StockResponse.class)
                    .block();

            if (stock == null || stock.getAvailableStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getName());
            }
        }

        // Order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDto item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtOrder(item.getPrice());
            order.addItem(orderItem);

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        // Payment
        ProcessPaymentRequest paymentRequest = new ProcessPaymentRequest();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setUserId(userId);
        paymentRequest.setAmount(total);
        paymentRequest.setPaymentMethod("CREDIT_CARD"); // Default

        try {
            PaymentResponse paymentResponse = webClientBuilder.build()
                    .post()
                    .uri("http://payment-service/api/payments")
                    .bodyValue(paymentRequest)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .block();

            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {
                savedOrder.setStatus(OrderStatus.PAID);
            } else {
                savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            }
        } catch (Exception e) {
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(savedOrder);

        // Clear cart (if payment successful or failed)
        webClientBuilder.build()
                .delete()
                .uri("http://shopping-cart-service/api/cart")
                .header("X-User-Id", userId.toString())
                .retrieve()
                .toBodilessEntity()
                .block();

        return mapToOrderResponse(savedOrder);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> {
                    OrderItemResponse itemResponse = new OrderItemResponse();
                    itemResponse.setProductId(item.getProductId());
                    itemResponse.setProductName(item.getProductName());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setPriceAtOrder(item.getPriceAtOrder());
                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(items);
        return response;
    }

    public List<OrderSummaryResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId);

        return orders.stream()
                .map(order -> {
                    OrderSummaryResponse response = new OrderSummaryResponse();
                    response.setOrderId(order.getId());
                    response.setOrderDate(order.getOrderDate());
                    response.setTotalAmount(order.getTotalAmount());
                    response.setStatus(order.getStatus());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this order");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse retryPayment(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Vérif commande appartient à l'utilisateur
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this order");
        }

        if (order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new RuntimeException("Payment retry only allowed for failed payments");
        }

        try {
            PaymentResponse paymentResponse = webClientBuilder.build()
                    .post()
                    .uri("http://payment-service/api/payments/{orderId}/retry", orderId)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .block();

            if (paymentResponse != null && "SUCCESS".equals(paymentResponse.getStatus())) {
                order.setStatus(OrderStatus.PAID);
            } else {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
            }
        } catch (Exception e) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }
}
