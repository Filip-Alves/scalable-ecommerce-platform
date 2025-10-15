package com.ecommerce.product_catalog_service.repository;

import com.ecommerce.product_catalog_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
