package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull BigDecimal finalPrice,
        String paymentMethod,
        String changedBy
) {}