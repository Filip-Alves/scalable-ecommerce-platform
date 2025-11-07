package com.ecommerce.product_catalog_service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockResponse {
    private Long productId;
    private Integer availableStock;
}