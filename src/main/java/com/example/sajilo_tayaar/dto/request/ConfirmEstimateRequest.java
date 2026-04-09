package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotNull;

public record ConfirmEstimateRequest(
        @NotNull Long userId,
        String changedBy
) {}