package com.ecommerce.shopping_cart_service.controller;


import com.ecommerce.shopping_cart_service.dto.CartItemResponse;
import com.ecommerce.shopping_cart_service.service.CartService;
import jakarta.validation.constraints.Null;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{userId}/{productId}")
    public Mono<ResponseEntity<Void>> addProductInCart(@PathVariable Long userId, @PathVariable Long productId) {
        return cartService.addProductToCart(userId, productId)
                .map(created -> created
                        ? ResponseEntity.status(HttpStatus.CREATED).build()
                        : ResponseEntity.noContent().build());
    }

    @GetMapping
    public Mono<ResponseEntity<List<CartItemResponse>>> getCart(
            @RequestHeader("X-User-Id") Long userId) {

        return cartService.getCartItems(userId)
                .map(ResponseEntity::ok);
    }

}
