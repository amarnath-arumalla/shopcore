package com.shopcore;

public class OutOfStockException extends ShopException {

    private final String sku;
    private final int available;
    private final int requested;

    public OutOfStockException(String sku, int available, int requested) {
        super("Not enough stock for " + sku + ": available " + available + ", requested " + requested);
        this.sku = sku;
        this.available = available;
        this.requested = requested;
    }

    public String getSku() { return sku; }
    public int getAvailable() { return available; }
    public int getRequested() { return requested; }
}
