package com.example.sajilo_tayaar.dto.response;

public record ShopListResponse(
        Long id,
        String name,
        String address,      // short formatted (for cards)
        String phone,

        Double avgRating,
        Integer ratingCount,

        Double lat,
        Double lng,

        Boolean verified
) {}