package com.shopcore;

import java.time.Instant;
import java.util.List;

public class Order {

    private final String orderId;
    private final List<OrderLine> lines;
    private final Money discount;
    private final String discountDescription;
    private final Instant createdAt;
    private OrderStatus status;

    public Order(String orderId, List<OrderLine> lines, Money discount, String discountDescription) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line");
        }
        if (discount == null) {
            throw new IllegalArgumentException("Discount is required (use Money.ZERO for none)");
        }
        if (discount.isGreaterThan(OrderLine.subtotalOf(lines))) {
            throw new IllegalArgumentException("Discount cannot be larger than the subtotal");
        }
        this.orderId = orderId;
        this.lines = List.copyOf(lines);
        this.discount = discount;
        this.discountDescription = discountDescription;
        this.createdAt = Instant.now();
        this.status = OrderStatus.PLACED;
    }

    public void pay() {
        requireStatus(OrderStatus.PLACED, "pay");
        status = OrderStatus.PAID;
    }

    public void ship() {
        requireStatus(OrderStatus.PAID, "ship");
        status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(orderId, status, "cancel");
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidOrderStateException(orderId, status, action);
        }
    }

    public Money getSubtotal() {
        return OrderLine.subtotalOf(lines);
    }

    public Money getTotal() {
        return getSubtotal().minus(discount);
    }

    public String getOrderId() { return orderId; }
    public List<OrderLine> getLines() { return lines; }
    public Money getDiscount() { return discount; }
    public String getDiscountDescription() { return discountDescription; }
    public Instant getCreatedAt() { return createdAt; }
    public OrderStatus getStatus() { return status; }
}
