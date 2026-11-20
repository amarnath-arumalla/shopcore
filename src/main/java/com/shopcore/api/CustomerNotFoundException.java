package com.shopcore.api;

public class CustomerNotFoundException extends RuntimeException {

    private final long customerId;

    public CustomerNotFoundException(long customerId) {
        super("No customer with id " + customerId);
        this.customerId = customerId;
    }

    public long getCustomerId() { return customerId; }
}
