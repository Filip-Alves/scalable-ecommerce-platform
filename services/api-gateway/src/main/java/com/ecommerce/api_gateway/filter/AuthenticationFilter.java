package com.ecommerce.api_gateway.filter;

import com.ecommerce.api_gateway.dto.ValidateResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    public AuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
            String token = authHeader.substring(7);

            return webClientBuilder.build()
                    .get()
                    .uri("http://user-service/api/users/validate?token=" + token)
                    .retrieve()
                    .bodyToMono(ValidateResponse.class)
                    .flatMap(validateResponse -> {
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", String.valueOf(validateResponse.userId()))
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    })
                    .onErrorResume(error -> onError(exchange, HttpStatus.UNAUTHORIZED));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}