package com.example.sajilo_tayaar.dto.response;

import java.time.Instant;

public record ShopAdminResponse(
        Long id,
        String name,
        String city,
        String phone,
        Boolean isActive,
        Boolean verified,
        Instant createdAt
) {}