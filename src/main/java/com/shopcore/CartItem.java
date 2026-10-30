package com.shopcore;

public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public void increaseQuantity(int extra) {
        if (extra <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        quantity = quantity + extra;
    }

    public Money getLineTotal() {
        return product.priceFor(quantity);
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}
