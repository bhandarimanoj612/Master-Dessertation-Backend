package com.example.sajilo_tayaar.dto.response;

public record ShopDetailsResponse(
        Long id,
        String name,

        String streetAddress,
        String area,
        String city,
        String state,
        String postalCode,

        String phone,
        String description,

        Double avgRating,
        Integer ratingCount,

        Double lat,
        Double lng,

        Boolean verified,
        Boolean isActive) {}