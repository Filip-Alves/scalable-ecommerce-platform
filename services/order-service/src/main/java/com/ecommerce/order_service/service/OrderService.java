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
        // 1. Get cart
        CartResponse cart = webClientBuilder.build()
                .get()
                .uri("http://shopping-cart-service/api/cart")
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(CartResponse.class)
                .block();

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 2. Check stock
        for (CartItemDto item : cart.getItems()) {
            StockResponse stock = webClientBuilder.build()
                    .get()
                    .uri("http://product-catalog-service/api/products/{id}/stock", item.getProductId())
                    .retrieve()
                    .bodyToMono(StockResponse.class)
                    .block();

            if (stock == null || stock.getAvailableStock() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
            }
        }

        // 3. Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDto item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtOrder(item.getPrice());
            order.addItem(orderItem);

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        // 4. Clear cart
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
}
