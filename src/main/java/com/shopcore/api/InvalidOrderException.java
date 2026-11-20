package com.shopcore.api;

/** Business rule broken by the request itself (not a single field). */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
