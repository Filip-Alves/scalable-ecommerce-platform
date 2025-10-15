package com.ecommerce.product_catalog_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank(message = "Le nom ne peut pas être vide.")
        @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
        String name,

        @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
        String description,

        @NotNull(message = "Le prix ne peut pas être nul.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif.")
        BigDecimal price
) {

}
