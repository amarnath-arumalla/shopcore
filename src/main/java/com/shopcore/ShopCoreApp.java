package com.shopcore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class ShopCoreApp {
    public static void main(String[] args) {
        CatalogFileStore store = new CatalogFileStore();
        Path catalogFile = Path.of("data", "catalog.csv");

        // 1. Build a catalog and save it
        ProductCatalog catalog = new ProductCatalog();
        catalog.addProduct(new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 10));
        catalog.addProduct(new Product("SKU-2001", "Clean Code", Category.BOOKS, Money.of("35.50"), 2));
        catalog.addProduct(new Product("SKU-3001", "Coffee Mug", Category.HOME, Money.of("12"), 25));

        try {
            store.save(catalog, catalogFile);
            System.out.println("Saved " + catalog.size() + " products to " + catalogFile);
            System.out.println("--- file contents ---");
            for (String line : Files.readAllLines(catalogFile)) {
                System.out.println(line);
            }
            System.out.println("---------------------");
        } catch (IOException e) {
            System.out.println("Could not save catalog: " + e.getMessage());
            return;
        }

        // 2. Load it back into a brand new catalog (like after a restart)
        try {
            ProductCatalog loaded = store.load(catalogFile);
            System.out.println("Loaded " + loaded.size() + " products");
            Product mug = loaded.getBySku("SKU-3001");
            System.out.println("Mug from file: " + mug + ", price " + mug.getPrice() + ", stock " + mug.getStock());
            System.out.println("Same product as before? " + mug.equals(catalog.getBySku("SKU-3001")));
        } catch (IOException e) {
            System.out.println("Could not load catalog: " + e.getMessage());
        }

        // 3. A missing file
        try {
            store.load(Path.of("data", "missing.csv"));
        } catch (NoSuchFileException e) {
            System.out.println("Missing file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Other file problem: " + e.getMessage());
        }

        // 4. A file with bad data on line 3
        Path badFile = Path.of("data", "bad.csv");
        try {
            Files.write(badFile, List.of(
                    "sku,name,category,price,stock",
                    "SKU-1001,Wireless Mouse,ELECTRONICS,19.99,10",
                    "SKU-2001,Clean Code,BOOK,35.50,2"));
            store.load(badFile);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad data: " + e.getMessage());
            System.out.println("Root cause: " + e.getCause().getClass().getSimpleName());
        } catch (IOException e) {
            System.out.println("Other file problem: " + e.getMessage());
        }
    }
}
