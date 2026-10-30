package com.shopcore;

public class PaymentFailedException extends ShopException {

    private final String orderId;
    private final String reason;

    public PaymentFailedException(String orderId, String reason) {
        super("Payment failed for order " + orderId + ": " + reason);
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
}
