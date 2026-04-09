package com.example.sajilo_tayaar.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {
    private Long shopId;
    private String name;
    private String sku;
    private String category;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
}
