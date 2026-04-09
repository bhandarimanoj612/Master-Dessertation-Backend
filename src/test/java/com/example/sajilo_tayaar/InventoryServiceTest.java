package com.example.sajilo_tayaar;

import com.example.sajilo_tayaar.dto.request.AdjustStockRequest;
import com.example.sajilo_tayaar.dto.request.CreateProductRequest;
import com.example.sajilo_tayaar.dto.request.UpdateProductRequest;
import com.example.sajilo_tayaar.entity.Product;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.repository.ProductRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.security.helper.AuthUser;
import com.example.sajilo_tayaar.services.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class  InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ShopRepository shopRepository;

    @InjectMocks private InventoryService inventoryService;

    private Product product;
    private Shop shop;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Sajilo Repair Center");

        product = new Product();
        product.setId(11L);
        product.setShop(shop);
        product.setName("Battery");
        product.setSku("BAT-1");
        product.setCategory("Parts");
        product.setSellingPrice(new BigDecimal("1000"));
        product.setCostPrice(new BigDecimal("700"));
        product.setStockQty(10);
        product.setActive(true);

        AuthUser authUser = new AuthUser(50L, "SHOP_OWNER", 1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authUser, null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProduct_shouldSaveNormalizedProduct() {
        CreateProductRequest req = new CreateProductRequest();
        req.setShopId(1L);
        req.setName("  Battery  ");
        req.setSku("  BAT-1  ");
        req.setCategory("  Parts  ");
        req.setSellingPrice(new BigDecimal("1000"));
        req.setCostPrice(new BigDecimal("700"));
        req.setStockQty(5);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.existsByShopIdAndSkuIgnoreCase(1L, "BAT-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = inventoryService.createProduct(req);

        assertEquals("Battery", saved.getName());
        assertEquals("BAT-1", saved.getSku());
        assertEquals("Parts", saved.getCategory());
        assertEquals(5, saved.getStockQty());
        assertTrue(saved.getActive());
    }

    @Test
    void createProduct_shouldRejectDuplicateSku() {
        CreateProductRequest req = new CreateProductRequest();
        req.setShopId(1L);
        req.setName("Battery");
        req.setSku(" BAT-1 ");

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.existsByShopIdAndSkuIgnoreCase(1L, "BAT-1")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventoryService.createProduct(req));

        assertEquals("SKU already exists in this shop", ex.getMessage());
    }

    @Test
    void adjustStock_shouldIncreaseStockForPositiveDelta() {
        AdjustStockRequest req = new AdjustStockRequest();
        req.setShopId(1L);
        req.setDelta(3);

        when(productRepository.findByIdAndShopId(11L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = inventoryService.adjustStock(11L, req);

        assertEquals(13, saved.getStockQty());
    }

    @Test
    void adjustStock_shouldRejectNegativeResultingStock() {
        AdjustStockRequest req = new AdjustStockRequest();
        req.setShopId(1L);
        req.setDelta(-11);

        when(productRepository.findByIdAndShopId(11L, 1L)).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventoryService.adjustStock(11L, req));

        assertEquals("Insufficient stock", ex.getMessage());
    }

    @Test
    void toggleProductActive_shouldFlipBooleanFlag() {
        when(productRepository.findByIdAndShopId(11L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = inventoryService.toggleProductActive(11L, 1L);

        assertFalse(saved.getActive());
    }

    @Test
    void updateProduct_shouldRejectDuplicateSkuOnAnotherProduct() {
        UpdateProductRequest req = new UpdateProductRequest(
                1L, "Battery Plus", " BAT-2 ", "Parts",
                new BigDecimal("1200"), new BigDecimal("800"), 9, true
        );

        when(productRepository.findByIdAndShopId(11L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByShopIdAndSkuIgnoreCaseAndIdNot(1L, "BAT-2", 11L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventoryService.updateProduct(11L, req));

        assertEquals("SKU already exists in this shop", ex.getMessage());
    }
}
