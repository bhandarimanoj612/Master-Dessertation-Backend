package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.AdjustStockRequest;
import com.example.sajilo_tayaar.dto.request.CreateProductRequest;
import com.example.sajilo_tayaar.dto.request.UpdateProductRequest;
import com.example.sajilo_tayaar.entity.Product;
import com.example.sajilo_tayaar.services.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {

    // Inventory endpoints back the stock management screen on the frontend.

    private final InventoryService inventoryService;

    @PostMapping("/products")
    public Product createProduct(@Valid @RequestBody CreateProductRequest req) {
        return inventoryService.createProduct(req);
    }

    @GetMapping("/products/shop/{shopId}")
    public List<Product> listProducts(@PathVariable Long shopId) {
        return inventoryService.getProductsByShop(shopId);
    }

    @GetMapping("/products/shop/{shopId}/{productId}")
    public Product getProduct(@PathVariable Long shopId, @PathVariable Long productId) {
        return inventoryService.getProductByShop(shopId, productId);
    }

    @PutMapping("/products/{productId}")
    public Product updateProduct(@PathVariable Long productId, @Valid @RequestBody UpdateProductRequest req) {
        return inventoryService.updateProduct(productId, req);
    }

    @PatchMapping("/products/{productId}/stock")
    public Product adjustStock(@PathVariable Long productId, @Valid @RequestBody AdjustStockRequest req) {
        return inventoryService.adjustStock(productId, req);
    }

    @PatchMapping("/products/{productId}/toggle-active")
    public Product toggleProductActive(@PathVariable Long productId, @RequestParam Long shopId) {
        return inventoryService.toggleProductActive(productId, shopId);
    }

    @DeleteMapping("/products/{productId}")
    public void deleteProduct(@PathVariable Long productId, @RequestParam Long shopId) {
        inventoryService.deleteProduct(productId, shopId);
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts();
    }
}
