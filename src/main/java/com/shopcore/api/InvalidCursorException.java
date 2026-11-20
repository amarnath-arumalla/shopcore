package com.shopcore.api;

public class InvalidCursorException extends RuntimeException {

    public InvalidCursorException() {
        super("The cursor is invalid or corrupted. Start again from the first page.");
    }
}