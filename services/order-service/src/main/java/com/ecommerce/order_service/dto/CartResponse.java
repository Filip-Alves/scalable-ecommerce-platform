package com.ecommerce.order_service.dto;

import java.util.List;

public class CartResponse {
    private Long userId;
    private List<CartItemDto> items;

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
}