package com.ecommerce.product_catalog_service.service;

import com.ecommerce.product_catalog_service.dto.CreateProductRequest;
import com.ecommerce.product_catalog_service.exception.ProductNotFoundException;
import com.ecommerce.product_catalog_service.model.Product;
import com.ecommerce.product_catalog_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(CreateProductRequest request) {
        Product newProduct = new Product();
        newProduct.setName(request.name());
        newProduct.setDescription(request.description());
        newProduct.setPrice(request.price());

        return productRepository.save(newProduct);
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
    }

    public void deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with id " + id);
        }

        productRepository.deleteById(id);
    }
}