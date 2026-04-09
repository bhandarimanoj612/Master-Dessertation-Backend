package com.example.sajilo_tayaar.dto.response;

import com.example.sajilo_tayaar.entity.enums.Role;

public record ShopUserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        Boolean isActive,
        Long shopId
) {}
