package com.shopcore;

import java.util.List;

public record OrderLine(String sku, String productName, Money unitPrice, int quantity) {

    public OrderLine {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (unitPrice == null || unitPrice.isZero()) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    public static Money subtotalOf(List<OrderLine> lines) {
        Money total = Money.ZERO;
        for (OrderLine line : lines) {
            total = total.plus(line.lineTotal());
        }
        return total;
    }
}
