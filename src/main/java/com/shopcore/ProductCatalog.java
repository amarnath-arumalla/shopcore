package com.shopcore;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProductCatalog {

    private final Map<String, Product> products = new HashMap<>();

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (products.containsKey(product.getSku())) {
            throw new DuplicateProductException(product.getSku());
        }
        products.put(product.getSku(), product);
    }

    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku));
    }

    public Product getBySku(String sku) {
        return findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public List<Product> findByCategory(Category category) {
        return products.values().stream()
                .filter(p -> p.getCategory() == category)
                .sorted(Comparator.comparing(p -> p.getName()))
                .toList();
    }

    public List<Product> findCheaperThan(Money maxPrice) {
        return products.values().stream()
                .filter(p -> p.getPrice().compareTo(maxPrice) < 0)
                .sorted(Comparator.comparing(p -> p.getPrice()))
                .toList();
    }

    public List<Product> findLowStock() {
        return products.values().stream()
                .filter(p -> p.isLowStock())
                .sorted(Comparator.comparing(p -> p.getStock()))
                .toList();
    }

    public List<Product> getAll() {
        return products.values().stream()
                .sorted(Comparator.comparing(p -> p.getSku()))
                .toList();
    }

    public int size() {
        return products.size();
    }
}
