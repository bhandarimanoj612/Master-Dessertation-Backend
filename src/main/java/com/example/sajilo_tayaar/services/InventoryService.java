package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.request.AdjustStockRequest;
import com.example.sajilo_tayaar.dto.request.CreateProductRequest;
import com.example.sajilo_tayaar.dto.request.UpdateProductRequest;
import com.example.sajilo_tayaar.entity.Product;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.repository.ProductRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.security.helper.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    // Inventory belongs to a shop, so write operations always check shop ownership first.

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    @Transactional
    public Product createProduct(CreateProductRequest req) {
        validateTenantAccess(req.getShopId());
        Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        if (hasText(req.getSku()) && productRepository.existsByShopIdAndSkuIgnoreCase(req.getShopId(), req.getSku().trim())) {
            throw new IllegalArgumentException("SKU already exists in this shop");
        }

        Product p = new Product();
        p.setShop(shop);
        p.setName(req.getName().trim());
        p.setSku(normalize(req.getSku()));
        p.setCategory(normalize(req.getCategory()));
        p.setSellingPrice(req.getSellingPrice());
        p.setCostPrice(req.getCostPrice());
        p.setStockQty(req.getStockQty() == null ? 0 : req.getStockQty());
        p.setActive(true);

        return productRepository.save(p);
    }

    public List<Product> getProductsByShop(Long shopId) {
        validateTenantAccess(shopId);
        return productRepository.findByShopId(shopId);
    }

    public Product getProductByShop(Long shopId, Long productId) {
        validateTenantAccess(shopId);
        return productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));
    }

    @Transactional
    public Product updateProduct(Long productId, UpdateProductRequest req) {
        validateTenantAccess(req.shopId());
        Product p = productRepository.findByIdAndShopId(productId, req.shopId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));

        if (hasText(req.sku()) && productRepository.existsByShopIdAndSkuIgnoreCaseAndIdNot(req.shopId(), req.sku().trim(), productId)) {
            throw new IllegalArgumentException("SKU already exists in this shop");
        }

        p.setName(req.name().trim());
        p.setSku(normalize(req.sku()));
        p.setCategory(normalize(req.category()));
        p.setSellingPrice(req.sellingPrice());
        p.setCostPrice(req.costPrice());
        p.setStockQty(req.stockQty() == null ? 0 : req.stockQty());
        if (req.active() != null) p.setActive(req.active());
        return productRepository.save(p);
    }

    @Transactional
    public Product adjustStock(Long productId, AdjustStockRequest req) {
        validateTenantAccess(req.getShopId());
        Product p = productRepository.findByIdAndShopId(productId, req.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));

        int next = p.getStockQty() + (req.getDelta() == null ? 0 : req.getDelta());
        if (next < 0) throw new IllegalArgumentException("Insufficient stock");

        p.setStockQty(next);
        return productRepository.save(p);
    }

    @Transactional
    public Product toggleProductActive(Long productId, Long shopId) {
        validateTenantAccess(shopId);
        Product p = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));
        p.setActive(!Boolean.TRUE.equals(p.getActive()));
        return productRepository.save(p);
    }

    @Transactional
    public void deleteProduct(Long productId, Long shopId) {
        validateTenantAccess(shopId);
        Product p = productRepository.findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));
        productRepository.delete(p);
    }

    public List<Product> getAllProducts() {
        String role = SecurityUtil.currentRole();
//        if (!"PLATFORM_ADMIN".equals(role)) {
//            throw new IllegalArgumentException("Only platform admin can access all products");
//        }
        return productRepository.findAll();
    }

    private void validateTenantAccess(Long shopId) {
        String role = SecurityUtil.currentRole();
//        if (role == null) {
//            throw new IllegalArgumentException("Authentication required");
//        }
//        if ("PLATFORM_ADMIN".equals(role)) {
//            return;
//        }
//        if (!("SHOP_OWNER".equals(role) || "SHOP_STAFF".equals(role) || "TECHNICIAN".equals(role))) {
//            throw new IllegalArgumentException("You are not allowed to manage shop inventory");
//        }
        Long tenantId = SecurityUtil.currentTenantId();
        if (tenantId == null || !tenantId.equals(shopId)) {
            throw new IllegalArgumentException("You can only manage your own shop inventory");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
