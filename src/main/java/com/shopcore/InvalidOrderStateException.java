package com.shopcore;

public class InvalidOrderStateException extends ShopException {

    private final String orderId;
    private final OrderStatus currentStatus;
    private final String action;

    public InvalidOrderStateException(String orderId, OrderStatus currentStatus, String action) {
        super("Cannot " + action + " order " + orderId + " because it is " + currentStatus);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
        this.action = action;
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getCurrentStatus() { return currentStatus; }
    public String getAction() { return action; }
}
