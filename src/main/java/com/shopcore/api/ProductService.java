package com.shopcore.api;

import java.util.List;
import java.util.Optional;

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

    public ProductResponse updateProduct(String sku, UpdateProductRequest request) {
        Optional<ProductResponse> updated = repository.update(sku, request);
        if (updated.isPresent()) {
            return updated.get();
        }

        // 0 rows changed: figure out why, only to send the right error
        ProductResponse current = repository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
        throw new StaleProductVersionException(sku, request.version(), current.version());
    }
}