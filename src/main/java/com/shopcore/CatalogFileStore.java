package com.shopcore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CatalogFileStore {

    private static final String HEADER = "sku,name,category,price,stock";

    public void save(ProductCatalog catalog, Path file) throws IOException {
        if (catalog == null || file == null) {
            throw new IllegalArgumentException("Catalog and file are required");
        }

        Path folder = file.toAbsolutePath().getParent();
        Files.createDirectories(folder);

        // Write to a temporary file first, then swap it in at the end.
        Path tempFile = folder.resolve(file.getFileName() + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (Product p : catalog.getAll()) {
                if (p.getName().contains(",")) {
                    throw new IllegalArgumentException(
                        "Product name cannot contain a comma: " + p.getName());
                }
                writer.write(p.getSku() + "," + p.getName() + "," + p.getCategory() + ","
                        + p.getPrice().amount() + "," + p.getStock());
                writer.newLine();
            }
        }

        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public ProductCatalog load(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is required");
        }

        ProductCatalog catalog = new ProductCatalog();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (!HEADER.equals(header)) {
                throw new IllegalArgumentException("Line 1: expected header '" + HEADER + "'");
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber = lineNumber + 1;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    catalog.addProduct(parseLine(line));
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(
                        "Line " + lineNumber + " (" + line + "): " + e.getMessage(), e);
                }
            }
        }
        return catalog;
    }

    private Product parseLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("expected 5 values but found " + parts.length);
        }
        String sku = parts[0].trim();
        String name = parts[1].trim();
        Category category = Category.valueOf(parts[2].trim());
        Money price = Money.of(parts[3].trim());
        int stock = Integer.parseInt(parts[4].trim());
        return new Product(sku, name, category, price, stock);
    }
}
