package com.ecommerce.shopping_cart_service.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        Integer quantity
) {}

