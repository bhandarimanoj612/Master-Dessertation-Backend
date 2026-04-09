package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTechnicianRequest(
        @NotNull Long shopId,
        @NotBlank String fullName,
        String phone,
        String specialization,
        String email,
        String password
) {}
