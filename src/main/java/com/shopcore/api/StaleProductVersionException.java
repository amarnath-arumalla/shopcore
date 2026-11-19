package com.shopcore.api;

public class StaleProductVersionException extends RuntimeException {

    private final String sku;
    private final int expectedVersion;
    private final int currentVersion;

    public StaleProductVersionException(String sku, int expectedVersion, int currentVersion) {
        super("Product '" + sku + "' was modified by someone else (you had version "
                + expectedVersion + ", current is " + currentVersion + "). Reload and try again.");
        this.sku = sku;
        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    public String getSku() { return sku; }
    public int getExpectedVersion() { return expectedVersion; }
    public int getCurrentVersion() { return currentVersion; }
}
