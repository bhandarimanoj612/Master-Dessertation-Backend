package com.example.sajilo_tayaar.dto.request;

import com.example.sajilo_tayaar.entity.enums.BookingType;
import com.example.sajilo_tayaar.entity.enums.ServiceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull Long shopId,
        Long customerUserId,

        @NotBlank String customerName,
        @NotBlank String customerPhone,
        String customerEmail,

        @NotNull BookingType bookingType,
        @NotNull ServiceMode serviceMode,

        @NotBlank String deviceCategory,
        String deviceModel,

        @NotBlank String issueDescription,
        String pickupAddress,

        LocalDateTime preferredDateTime
) {}