package com.example.sajilo_tayaar.dto.request;

import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull RepairStatus status,
        String changedBy,
        String remarks
) {}