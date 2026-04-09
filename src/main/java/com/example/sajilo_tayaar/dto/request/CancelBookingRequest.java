package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotNull;

public record CancelBookingRequest(
        @NotNull Long userId,
        String changedBy,
        String remarks
) {}