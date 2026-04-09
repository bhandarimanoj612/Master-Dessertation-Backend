package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.request.CreateInvoiceRequest;
import com.example.sajilo_tayaar.entity.*;
import com.example.sajilo_tayaar.repository.InvoiceRepository;
import com.example.sajilo_tayaar.repository.ProductRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PosService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(CreateInvoiceRequest req) {
        Shop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        if (req.getItems() == null || req.getItems().isEmpty())
            throw new IllegalArgumentException("Invoice items required");

        Invoice invoice = new Invoice();
        invoice.setShop(shop);

        // ── FIX: populate tenant_id ──────────────────────────────────────────
        // Option A (preferred if Shop has tenantId): invoice.setTenantId(shop.getTenantId());
        // Option B (if shopId == tenantId in your schema):
        Long tenantId = req.getTenantId() != null ? req.getTenantId() : req.getShopId();
        invoice.setTenantId(tenantId);
        // ─────────────────────────────────────────────────────────────────────

        invoice.setCustomerName(req.getCustomerName());
        invoice.setCustomerPhone(req.getCustomerPhone());
        invoice.setPaymentMethod(req.getPaymentMethod() == null ? "CASH" : req.getPaymentMethod());
        invoice.setDiscount(req.getDiscount() == null ? BigDecimal.ZERO : req.getDiscount());
        invoice.setTax(req.getTax() == null ? BigDecimal.ZERO : req.getTax());

        BigDecimal subTotal = BigDecimal.ZERO;

        for (CreateInvoiceRequest.Item it : req.getItems()) {
            Product product = productRepository.findByIdAndShopId(it.getProductId(), req.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found in this shop"));

            int qty = it.getQty() == null ? 0 : it.getQty();
            if (qty <= 0) throw new IllegalArgumentException("Invalid qty");

            if (product.getStockQty() < qty)
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());

            BigDecimal unitPrice = (it.getUnitPrice() != null) ? it.getUnitPrice() : product.getSellingPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            product.setStockQty(product.getStockQty() - qty);
            productRepository.save(product);

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQty(qty);
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);

            invoice.getItems().add(item);
            subTotal = subTotal.add(lineTotal);
        }

        invoice.setSubTotal(subTotal);

        BigDecimal grand = subTotal
                .subtract(invoice.getDiscount())
                .add(invoice.getTax());
        if (grand.compareTo(BigDecimal.ZERO) < 0) grand = BigDecimal.ZERO;
        invoice.setGrandTotal(grand);

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> listInvoices(Long shopId) {
        return invoiceRepository.findByShopIdOrderByIdDesc(shopId);
    }

    public Invoice getInvoice(Long shopId, Long invoiceId) {
        return invoiceRepository.findByIdAndShopId(invoiceId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}