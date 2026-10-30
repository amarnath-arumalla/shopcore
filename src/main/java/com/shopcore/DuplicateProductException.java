package com.shopcore;

public class DuplicateProductException extends ShopException {

    private final String sku;

    public DuplicateProductException(String sku) {
        super("A product with SKU " + sku + " already exists");
        this.sku = sku;
    }

    public String getSku() { return sku; }
}
