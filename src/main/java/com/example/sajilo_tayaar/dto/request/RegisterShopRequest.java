package com.example.sajilo_tayaar.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterShopRequest(

        @NotBlank(message = "Shop name is required")
        String shopName,

        @NotBlank(message = "Shop street address is required")
        String shopStreetAddress,

        @NotBlank(message = "Area is required")
        String area,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        @NotBlank(message = "Shop phone is required")
        String shopPhone,

        String description,

        @NotNull(message = "Latitude is required")
        Double lat,

        @NotNull(message = "Longitude is required")
        Double lng,

        @NotBlank(message = "Owner full name is required")
        String ownerFullName,

        @Email(message = "Valid owner email is required")
        @NotBlank(message = "Owner email is required")
        String ownerEmail,

        @NotBlank(message = "Owner password is required")
        String ownerPassword,

        @NotBlank(message = "Owner phone is required")
        String ownerPhone
) {}