package com.shopcore.api;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private static final int MAX_PRODUCTS = 50;

    private final ProductCatalogRepository repository;

    public ProductService(ProductCatalogRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponse> listProducts() {
        return repository.findFirst(MAX_PRODUCTS);
    }

    public ProductResponse getProduct(String sku) {
        return repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        return repository.insert(request);
    }
}