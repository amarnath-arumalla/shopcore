package com.shopcore;

public class FraudSuspectedException extends ShopException {

    private final String orderId;

    public FraudSuspectedException(String orderId) {
        super("Order " + orderId + " was flagged by the fraud check");
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
}
