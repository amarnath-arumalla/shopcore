package com.shopcore;

import java.util.Objects;

public class Product {

    private final String sku;
    private final String name;
    private final Category category;
    private final Money price;
    private int stock;

    public Product(String sku, String name, Category category, Money price, int stock) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (price == null || price.isZero()) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public void addStock(int quantity) {
        requirePositive(quantity);
        stock = stock + quantity;
    }

    public void removeStock(int quantity) {
        requirePositive(quantity);
        if (quantity > stock) {
            throw new OutOfStockException(sku, stock, quantity);
        }
        stock = stock - quantity;
    }

    public Money priceFor(int quantity) {
        requirePositive(quantity);
        return price.times(quantity);
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public boolean isLowStock() {
        return stock <= 3;
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    // Two Product objects are "the same product" if they have the same SKU.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product)) {
            return false;
        }
        Product that = (Product) other;
        return sku.equals(that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return name + " (" + sku + ")";
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public Money getPrice() { return price; }
    public int getStock() { return stock; }
}
