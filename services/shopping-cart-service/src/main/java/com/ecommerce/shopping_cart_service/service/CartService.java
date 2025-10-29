package com.ecommerce.shopping_cart_service.service;

import com.ecommerce.shopping_cart_service.dto.ProductDTO;
import com.ecommerce.shopping_cart_service.model.CartItem;
import com.ecommerce.shopping_cart_service.repository.CartItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Service
public class CartService {

    private final WebClient.Builder webClientBuilder;
    private final CartItemRepository cartItemRepository;

    public CartService(WebClient.Builder webClientBuilder, CartItemRepository cartItemRepository) {
        this.webClientBuilder = webClientBuilder;
        this.cartItemRepository = cartItemRepository;
    }

    public Mono<Boolean> addProductToCart(Long userId, Long productId) {
        return webClientBuilder.build()
                .get()
                .uri("http://product-catalog-service/api/products/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        r -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé")))
                .bodyToMono(ProductDTO.class)
                .flatMap(p -> Mono.fromCallable(() -> {
                    Optional<CartItem> optional = cartItemRepository.findByCartIdAndProductId(userId, productId);

                    CartItem item = optional.orElseGet(() -> {
                        CartItem newItem = new CartItem();
                        newItem.setCartId(userId);
                        newItem.setProductId(productId);
                        newItem.setQuantity(0);
                        return newItem;
                    });

                    boolean isNew = optional.isEmpty();

                    item.setQuantity(item.getQuantity() + 1);
                    cartItemRepository.save(item);
                    return isNew;
                }));
    }

}
