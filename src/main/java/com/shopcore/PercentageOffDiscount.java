package com.shopcore;

import java.util.List;

public class PercentageOffDiscount implements DiscountRule {

    private final int percent;
    private final Money minimumSubtotal;

    public PercentageOffDiscount(int percent, Money minimumSubtotal) {
        if (percent < 1 || percent > 90) {
            throw new IllegalArgumentException("Percent must be between 1 and 90");
        }
        if (minimumSubtotal == null) {
            throw new IllegalArgumentException("Minimum subtotal is required");
        }
        this.percent = percent;
        this.minimumSubtotal = minimumSubtotal;
    }

    @Override
    public Money discountFor(List<OrderLine> lines) {
        Money subtotal = OrderLine.subtotalOf(lines);
        if (minimumSubtotal.isGreaterThan(subtotal)) {
            return Money.ZERO;
        }
        return subtotal.percent(percent);
    }

    @Override
    public String getDescription() {
        return percent + "% off orders over " + minimumSubtotal;
    }
}
