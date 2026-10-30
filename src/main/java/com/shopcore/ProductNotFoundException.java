package com.shopcore;

public class ProductNotFoundException extends ShopException {

    private final String sku;

    public ProductNotFoundException(String sku) {
        super("No product with SKU " + sku);
        this.sku = sku;
    }

    public String getSku() { return sku; }
}
