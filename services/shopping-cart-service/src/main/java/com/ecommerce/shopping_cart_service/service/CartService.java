package com.ecommerce.shopping_cart_service.service;

import com.ecommerce.shopping_cart_service.dto.CartItemResponse;
import com.ecommerce.shopping_cart_service.dto.ProductDTO;
import com.ecommerce.shopping_cart_service.model.CartItem;
import com.ecommerce.shopping_cart_service.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
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


    public void removeItem(Long userId, Long productId) {
        cartItemRepository.deleteByCartIdAndProductId(userId, productId);
    }

    public CartItemResponse updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByCartIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        ProductDTO product = webClientBuilder.build()
                .get()
                .uri("http://product-catalog-service/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();

        return new CartItemResponse(
                item.getProductId(),
                product.name(),
                product.price(),
                item.getQuantity()
        );
    }

    public List<CartItemResponse> getCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(userId);

        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = cartItems.stream()
                .map(CartItem::getProductId)
                .toList();

        String idsParam = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        List<ProductDTO> products = webClientBuilder.build()
                .get()
                .uri("http://product-catalog-service/api/products?ids={ids}", idsParam)
                .retrieve()
                .bodyToFlux(ProductDTO.class)
                .collectList()
                .block();

        Map<Long, ProductDTO> productMap = products.stream()
                .collect(Collectors.toMap(ProductDTO::id, p -> p));

        return cartItems.stream()
                .map(item -> {
                    ProductDTO product = productMap.get(item.getProductId());
                    return new CartItemResponse(
                            item.getProductId(),
                            product.name(),
                            product.price(),
                            item.getQuantity()
                    );
                })
                .toList();
    }
}
