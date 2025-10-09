package com.ecommerce.product_catalog_service.controller;

import com.ecommerce.product_catalog_service.dto.CreateProductRequest;
import com.ecommerce.product_catalog_service.model.Product;
import com.ecommerce.product_catalog_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productService.getProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product savedProduct = productService.createProduct(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedProduct);
    }
}