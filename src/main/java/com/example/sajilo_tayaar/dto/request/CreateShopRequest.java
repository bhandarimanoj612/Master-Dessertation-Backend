package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateShopRequest(

        @NotBlank
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