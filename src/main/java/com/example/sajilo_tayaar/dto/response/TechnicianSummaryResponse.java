package com.example.sajilo_tayaar.dto.response;

public record TechnicianSummaryResponse(
        Long id,
        String fullName,
        String phone,
        String specialization
) {}