package com.shopcore.api;

public class DuplicateSkuException extends RuntimeException {

    private final String sku;

    public DuplicateSkuException(String sku, Throwable cause) {
        super("A product with SKU '" + sku + "' already exists");
        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }
}