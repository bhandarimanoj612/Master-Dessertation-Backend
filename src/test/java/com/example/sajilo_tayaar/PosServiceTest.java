package com.example.sajilo_tayaar;

import com.example.sajilo_tayaar.dto.request.CreateInvoiceRequest;
import com.example.sajilo_tayaar.entity.Invoice;
import com.example.sajilo_tayaar.entity.Product;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.repository.InvoiceRepository;
import com.example.sajilo_tayaar.repository.ProductRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.services.PosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosServiceTest {

    @Mock private ShopRepository shopRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks private PosService posService;

    private Shop shop;
    private Product product;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Sajilo Repair Center");

        product = new Product();
        product.setId(101L);
        product.setShop(shop);
        product.setName("Screen Protector");
        product.setSellingPrice(new BigDecimal("500"));
        product.setStockQty(10);
    }

    private CreateInvoiceRequest baseRequest() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setShopId(1L);
        req.setTenantId(1L);
        req.setCustomerName("Manoj");
        req.setCustomerPhone("9800");
        req.setPaymentMethod("CASH");

        CreateInvoiceRequest.Item item = new CreateInvoiceRequest.Item();
        item.setProductId(101L);
        item.setQty(2);
        item.setUnitPrice(new BigDecimal("500"));
        req.setItems(List.of(item));
        return req;
    }

    @Test
    void createInvoice_shouldCalculateSubtotalCorrectly() {
        CreateInvoiceRequest req = baseRequest();
        req.setDiscount(BigDecimal.ZERO);
        req.setTax(BigDecimal.ZERO);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.findByIdAndShopId(101L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice invoice = posService.createInvoice(req);

        assertEquals(new BigDecimal("1000"), invoice.getSubTotal());
        assertEquals(new BigDecimal("1000"), invoice.getGrandTotal());
    }

    @Test
    void createInvoice_shouldApplyDiscountAndTaxToGrandTotal() {
        CreateInvoiceRequest req = baseRequest();
        req.setDiscount(new BigDecimal("100"));
        req.setTax(new BigDecimal("50"));

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.findByIdAndShopId(101L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice invoice = posService.createInvoice(req);

        assertEquals(new BigDecimal("950"), invoice.getGrandTotal());
    }

    @Test
    void createInvoice_shouldUseProductSellingPriceWhenUnitPriceMissing() {
        CreateInvoiceRequest req = baseRequest();
        req.getItems().get(0).setUnitPrice(null);
        req.setDiscount(BigDecimal.ZERO);
        req.setTax(BigDecimal.ZERO);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.findByIdAndShopId(101L, 1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice invoice = posService.createInvoice(req);

        assertEquals(new BigDecimal("1000"), invoice.getSubTotal());
        assertEquals(8, product.getStockQty());
    }

    @Test
    void createInvoice_shouldRejectInsufficientStock() {
        CreateInvoiceRequest req = baseRequest();
        req.getItems().get(0).setQty(50);

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(productRepository.findByIdAndShopId(101L, 1L)).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> posService.createInvoice(req));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void createInvoice_shouldRejectEmptyItems() {
        CreateInvoiceRequest req = new CreateInvoiceRequest();
        req.setShopId(1L);
        req.setItems(List.of());

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> posService.createInvoice(req));

        assertEquals("Invoice items required", ex.getMessage());
    }
}
