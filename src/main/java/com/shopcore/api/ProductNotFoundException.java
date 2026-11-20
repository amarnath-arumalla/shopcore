package com.shopcore.api;

public class ProductNotFoundException extends RuntimeException {

    private final String sku;

    public ProductNotFoundException(String sku) {
        super("No product with SKU '" + sku + "'");
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}