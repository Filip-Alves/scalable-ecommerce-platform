package com.ecommerce.shopping_cart_service.repository;

import com.ecommerce.shopping_cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long userId, Long productId);
}
