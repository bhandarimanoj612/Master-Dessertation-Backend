package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignTechnicianRequest(
        @NotNull Long technicianId,
        String changedBy,
        String remarks
) {}