package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotNull Long shopId,
        @NotBlank String name,
        String sku,
        String category,
        @NotNull @DecimalMin("0.0") BigDecimal sellingPrice,
        @DecimalMin("0.0") BigDecimal costPrice,
        Integer stockQty,
        Boolean active
) {
}
