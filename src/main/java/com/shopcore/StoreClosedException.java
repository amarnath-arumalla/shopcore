package com.shopcore;

public class StoreClosedException extends ShopException {

    public StoreClosedException() {
        super("The store is closed for maintenance. Please try again later.");
    }
}
