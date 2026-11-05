package com.shopcore;

public class StoreStatus {

    // volatile: when an admin closes the store, every checkout thread sees it immediately.
    private volatile boolean open = true;

    public void close() {
        open = false;
    }

    public void reopen() {
        open = true;
    }

    public boolean isOpen() {
        return open;
    }
}