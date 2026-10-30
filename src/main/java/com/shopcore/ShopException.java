package com.shopcore;

public abstract class ShopException extends RuntimeException {

    protected ShopException(String message) {
        super(message);
    }
}
