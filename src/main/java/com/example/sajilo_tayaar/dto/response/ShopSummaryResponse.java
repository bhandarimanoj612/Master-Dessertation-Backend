package com.example.sajilo_tayaar.dto.response;

public record ShopSummaryResponse(
        Long id,
        String name,
        String streetAddress,
        String area,
        String city,
        String state,
        String postalCode,
        String phone,
        Double avgRating,
        Integer ratingCount,
        Boolean isActive,
        java.time.Instant createdAt,
        Double lat,
        Double lng,
        Boolean verified,
        String description
) {}
