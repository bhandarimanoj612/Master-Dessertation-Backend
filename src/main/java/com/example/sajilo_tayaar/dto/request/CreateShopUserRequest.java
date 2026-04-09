package com.example.sajilo_tayaar.dto.request;

import com.example.sajilo_tayaar.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShopUserRequest(
        @NotNull Long shopId,
        @NotBlank String fullName,
        @Email @NotBlank String email,
        String phone,
        @NotBlank String password,
        @NotNull Role role
) {}
