package com.example.sajilo_tayaar.dto.request;

import com.example.sajilo_tayaar.entity.enums.Role;

public record UpdateUserRequest(
        String fullName,
        String email,
        String phone,
        Role role
) {}