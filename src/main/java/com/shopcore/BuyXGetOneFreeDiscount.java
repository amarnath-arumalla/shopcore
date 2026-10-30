package com.shopcore;

import java.util.List;

public class BuyXGetOneFreeDiscount implements DiscountRule {

    private final String sku;
    private final int buyQuantity;

    public BuyXGetOneFreeDiscount(String sku, int buyQuantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (buyQuantity < 1) {
            throw new IllegalArgumentException("Buy quantity must be at least 1");
        }
        this.sku = sku;
        this.buyQuantity = buyQuantity;
    }

    @Override
    public Money discountFor(List<OrderLine> lines) {
        for (OrderLine line : lines) {
            if (line.sku().equals(sku)) {
                int freeItems = line.quantity() / (buyQuantity + 1);
                return line.unitPrice().times(freeItems);
            }
        }
        return Money.ZERO;
    }

    @Override
    public String getDescription() {
        return "Buy " + buyQuantity + " get 1 free on " + sku;
    }
}
