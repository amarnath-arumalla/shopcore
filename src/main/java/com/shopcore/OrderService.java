package com.shopcore;

import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final ProductCatalog catalog;
    private int nextOrderNumber = 1001;

    public OrderService(ProductCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog is required");
        }
        this.catalog = catalog;
    }

    public Order placeOrder(ShoppingCart cart) {
        return placeOrder(cart, List.of());
    }

    public synchronized Order placeOrder(ShoppingCart cart, List<DiscountRule> discountRules) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart");
        }
        if (discountRules == null) {
            throw new IllegalArgumentException("Discount rules are required (use List.of() for none)");
        }

        // Step 1: check EVERY item first. Change nothing yet.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStock()) {
                throw new OutOfStockException(product.getSku(), product.getStock(), item.getQuantity());
            }
        }

        // Step 2: copy each line (price snapshot). Still nothing changed.
        List<OrderLine> lines = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            lines.add(new OrderLine(product.getSku(), product.getName(),
                    product.getPrice(), item.getQuantity()));
        }

        // Step 3: pick the single best discount. Discounts do not stack.
        DiscountRule bestRule = null;
        Money bestDiscount = Money.ZERO;
        for (DiscountRule rule : discountRules) {
            Money discount = rule.discountFor(lines);
            if (discount.isGreaterThan(bestDiscount)) {
                bestRule = rule;
                bestDiscount = discount;
            }
        }
        String description = (bestRule == null) ? "No discount" : bestRule.getDescription();

        // Step 4: build the order. If this throws, stock is still untouched.
        Order order = new Order("ORD-" + nextOrderNumber, lines, bestDiscount, description);

        // Step 5: everything is valid, so now change state.
        for (CartItem item : cart.getItems()) {
            item.getProduct().removeStock(item.getQuantity());
        }
        nextOrderNumber = nextOrderNumber + 1;
        cart.clear();
        return order;
    }

    public synchronized void cancelOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        // Change the status first. If the order cannot be cancelled, this throws
        // and we never touch the stock.
        order.cancel();

        // Put every item back on the shelf.
        for (OrderLine line : order.getLines()) {
            Product product = catalog.getBySku(line.sku());
            product.addStock(line.quantity());
        }
    }

    public PaymentResult payOrder(Order order, PaymentMethod paymentMethod) {
        if (order == null || paymentMethod == null) {
            throw new IllegalArgumentException("Order and payment method are required");
        }

        // Step 1: make sure the order can be paid BEFORE taking any money.
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new InvalidOrderStateException(order.getOrderId(), order.getStatus(), "pay");
        }

        // Step 2: charge. We don't know or care which kind of payment this is.
        PaymentResult result = paymentMethod.charge(order.getTotal());

        // Step 3: only mark the order paid if the money actually came through.
        if (!result.success()) {
            throw new PaymentFailedException(order.getOrderId(), result.message());
        }
        order.pay();
        return result;
    }
}
