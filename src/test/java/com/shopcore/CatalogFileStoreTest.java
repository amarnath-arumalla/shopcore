package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CatalogFileStoreTest {

    @TempDir
    Path tempDir;

    private final CatalogFileStore store = new CatalogFileStore();

    @Test
    void savedCatalogLoadsBackTheSame() throws IOException {
        ProductCatalog catalog = new ProductCatalog();
        catalog.addProduct(new Product("SKU-3001", "Coffee Mug", Category.HOME, Money.of("12"), 25));
        Path file = tempDir.resolve("catalog.csv");

        store.save(catalog, file);
        ProductCatalog loaded = store.load(file);

        Product mug = loaded.getBySku("SKU-3001");
        assertEquals(Money.of("12.00"), mug.getPrice());
        assertEquals(25, mug.getStock());
    }

    @Test
    void badLineReportsItsLineNumber() throws IOException {
        Path file = tempDir.resolve("bad.csv");
        Files.write(file, List.of(
                "sku,name,category,price,stock",
                "SKU-1001,Wireless Mouse,ELECTRONICS,19.99,10",
                "SKU-2001,Clean Code,BOOK,35.50,2"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> store.load(file));

        assertTrue(e.getMessage().startsWith("Line 3"));
    }

    @Test
    void missingFileThrowsNoSuchFile() {
        assertThrows(NoSuchFileException.class, () -> store.load(tempDir.resolve("missing.csv")));
    }
}
