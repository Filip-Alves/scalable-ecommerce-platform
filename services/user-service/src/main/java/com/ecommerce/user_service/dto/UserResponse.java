package com.ecommerce.user_service.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserResponse (
        @Id Long id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email
) {
}
