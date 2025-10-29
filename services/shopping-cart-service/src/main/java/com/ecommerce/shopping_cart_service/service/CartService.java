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
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit non trouvé")))
                .bodyToMono(ProductDTO.class)
                .flatMap(product -> {
                    return Mono.fromCallable(() -> {
                        Optional<CartItem> optionalItem = cartItemRepository.findByCartIdAndProductId(userId, productId);
                        CartItem item;
                        boolean isNew = false;
                        if (optionalItem.isPresent()) {
                            item = optionalItem.get();
                            item.setQuantity(item.getQuantity() + 1);
                        } else {
                            item = new CartItem();
                            item.setCartId(userId);
                            item.setProductId(productId);
                            item.setQuantity(1);
                            isNew = true;
                        }
                        cartItemRepository.save(item);
                        return isNew;
                    });
                });
    }
}
