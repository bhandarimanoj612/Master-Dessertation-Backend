package com.example.sajilo_tayaar.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateInvoiceRequest {
    private Long shopId;

    /**
     * tenantId — sent from the frontend alongside shopId.
     * In most SaaS setups shopId == tenantId, but the DB enforces
     * tenant_id as a separate NOT NULL column, so we carry it explicitly.
     */
    private Long tenantId;

    private String customerName;
    private String customerPhone;
    private String paymentMethod; // CASH | CARD | ONLINE
    private BigDecimal discount;
    private BigDecimal tax;
    private String notes;
    private List<Item> items;

    @Getter @Setter
    public static class Item {
        private Long productId;
        private Integer qty;
        private BigDecimal unitPrice; // optional — falls back to product.sellingPrice
    }
}