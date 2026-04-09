package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProvideEstimateRequest(
        @NotNull BigDecimal estimatedPrice,
        String technicianNote,
        String changedBy
) {}