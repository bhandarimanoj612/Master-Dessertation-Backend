package com.example.sajilo_tayaar.dto.response;

import com.example.sajilo_tayaar.entity.Invoice;
import com.example.sajilo_tayaar.entity.InvoiceItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class InvoiceResponse {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal grandTotal;
    private LocalDateTime createdAt;
    private List<ItemResponse> items;

    public static InvoiceResponse from(Invoice inv) {
        return InvoiceResponse.builder()
                .id(inv.getId())
                .customerName(inv.getCustomerName())
                .customerPhone(inv.getCustomerPhone())
                .paymentMethod(inv.getPaymentMethod())
                .subTotal(inv.getSubTotal())
                .discount(inv.getDiscount())
                .tax(inv.getTax())
                .grandTotal(inv.getGrandTotal())
                .createdAt(inv.getCreatedAt())
                .items(inv.getItems().stream().map(ItemResponse::from).collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Builder
    public static class ItemResponse {
        private Long id;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private ProductSummary product;

        public static ItemResponse from(InvoiceItem item) {
            return ItemResponse.builder()
                    .id(item.getId())
                    .qty(item.getQty())
                    .unitPrice(item.getUnitPrice())
                    .lineTotal(item.getLineTotal())
                    .product(ProductSummary.from(item))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductSummary {
        private Long id;
        private String name;
        private String sku;
        private String category;
        private BigDecimal sellingPrice;
        private Integer stockQty;

        public static ProductSummary from(InvoiceItem item) {
            var p = item.getProduct();
            return ProductSummary.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .sku(p.getSku())
                    .category(p.getCategory())
                    .sellingPrice(p.getSellingPrice())
                    .stockQty(p.getStockQty())
                    .build();
        }
    }
}