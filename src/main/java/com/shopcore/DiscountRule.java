package com.shopcore;

import java.util.List;

public interface DiscountRule {

    Money discountFor(List<OrderLine> lines);

    String getDescription();
}
