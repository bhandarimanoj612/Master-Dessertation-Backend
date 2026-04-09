package com.example.sajilo_tayaar.dto.response;

public record AuthResponse(
        String token,
        Long userId,
        String role,
        String email,
        String phoneNumber,
        Long tenantId,
        String fullName,
        AuthShopResponse shop
) {}