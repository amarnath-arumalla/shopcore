package com.shopcore.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        return productService.listProducts();
    }

    @GetMapping("/{sku}")
    public ProductResponse getProduct(@PathVariable String sku) {
        return productService.getProduct(sku);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{sku}")
                .buildAndExpand(created.sku())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{sku}")
    public ProductResponse updateProduct(@PathVariable String sku,
                                         @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(sku, request);    // 200 + new version
    }
}