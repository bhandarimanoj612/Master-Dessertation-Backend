package com.example.sajilo_tayaar.dto.response;

import com.example.sajilo_tayaar.entity.enums.BookingType;
import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import com.example.sajilo_tayaar.entity.enums.ServiceMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record BookingDetailsResponse(
        Long id,
        ShopSummaryResponse shop,
        TechnicianSummaryResponse assignedTechnician,
        Long customerUserId,
        String customerName,
        String customerPhone,
        String customerEmail,
        BookingType bookingType,
        ServiceMode serviceMode,
        String deviceCategory,
        String deviceModel,
        String issueDescription,
        String pickupAddress,
        LocalDateTime preferredDateTime,
        RepairStatus repairStatus,
        BigDecimal estimatedPrice,
        String technicianNote,
        Boolean customerApprovedEstimate,
        BigDecimal finalPrice,
        Boolean paid,
        String paymentMethod,
        Instant paidAt,
        Instant createdAt
) {}
