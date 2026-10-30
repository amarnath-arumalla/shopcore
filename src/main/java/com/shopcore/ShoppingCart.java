package com.shopcore;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CartItem existing = findItem(product.getSku());
        int alreadyInCart = (existing == null) ? 0 : existing.getQuantity();

        if (alreadyInCart + quantity > product.getStock()) {
            throw new OutOfStockException(product.getSku(), product.getStock(), alreadyInCart + quantity);
        }

        if (existing == null) {
            items.add(new CartItem(product, quantity));
        } else {
            existing.increaseQuantity(quantity);
        }
    }

    public void removeItem(String sku) {
        CartItem existing = findItem(sku);
        if (existing == null) {
            throw new IllegalArgumentException("Item " + sku + " is not in the cart");
        }
        items.remove(existing);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Money getTotal() {
        Money total = Money.ZERO;
        for (CartItem item : items) {
            total = total.plus(item.getLineTotal());
        }
        return total;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count = count + item.getQuantity();
        }
        return count;
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    private CartItem findItem(String sku) {
        for (CartItem item : items) {
            if (item.getProduct().getSku().equals(sku)) {
                return item;
            }
        }
        return null;
    }
}
