package com.example.sajilo_tayaar.dto.request;

public record UpdateShopRequest(
        String name,

        String streetAddress,
        String area,
        String city,
        String state,
        String postalCode,

        String phone,
        String description,

        Double lat,
        Double lng
) {}