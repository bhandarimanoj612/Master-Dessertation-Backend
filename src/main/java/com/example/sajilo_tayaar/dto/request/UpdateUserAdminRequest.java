package com.example.sajilo_tayaar.dto.request;

import com.example.sajilo_tayaar.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserAdminRequest(
        @NotNull Role role,
        Long tenantId,       // nullable allowed for CUSTOMER
        Boolean isActive
) {}
