package com.ecommerce.shopping_cart_service.controller;


import com.ecommerce.shopping_cart_service.dto.CartItemResponse;
import com.ecommerce.shopping_cart_service.dto.UpdateQuantityRequest;
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

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {

        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId,
            @RequestBody UpdateQuantityRequest request) {

        CartItemResponse response = cartService.updateQuantity(userId, productId, request.quantity());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @RequestHeader("X-User-Id") Long userId) {

        List<CartItemResponse> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }

}
