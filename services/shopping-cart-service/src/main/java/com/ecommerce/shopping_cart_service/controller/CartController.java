package com.ecommerce.shopping_cart_service.controller;


import com.ecommerce.shopping_cart_service.service.CartService;
import jakarta.validation.constraints.Null;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/cart/")
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


}
