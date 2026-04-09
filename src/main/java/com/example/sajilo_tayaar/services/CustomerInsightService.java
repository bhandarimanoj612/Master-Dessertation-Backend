package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.response.ShopCustomerSummaryResponse;
import com.example.sajilo_tayaar.entity.Booking;
import com.example.sajilo_tayaar.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerInsightService {

    // Customer summaries are built from booking history because customers are not shop-owned accounts.

    private final BookingRepository bookingRepository;

    public List<ShopCustomerSummaryResponse> getCustomersForShop(Long shopId) {
        List<Booking> bookings = bookingRepository.findByShopId(shopId);
        Map<String, ShopCustomerAccumulator> grouped = new LinkedHashMap<>();

        for (Booking booking : bookings) {
            String key = buildCustomerKey(booking);
            ShopCustomerAccumulator acc = grouped.computeIfAbsent(key, unused -> new ShopCustomerAccumulator());
            acc.customerUserId = booking.getCustomerUserId();
            acc.customerName = safe(booking.getCustomerName());
            acc.customerPhone = safe(booking.getCustomerPhone());
            acc.customerEmail = safe(booking.getCustomerEmail());
            acc.totalRepairs++;
            if (booking.getFinalPrice() != null) {
                acc.totalSpent = acc.totalSpent.add(booking.getFinalPrice());
            } else if (booking.getEstimatedPrice() != null && Boolean.TRUE.equals(booking.getPaid())) {
                acc.totalSpent = acc.totalSpent.add(booking.getEstimatedPrice());
            }
            if (booking.getCreatedAt() != null && (acc.lastVisit == null || booking.getCreatedAt().isAfter(acc.lastVisit))) {
                acc.lastVisit = booking.getCreatedAt();
                acc.latestRepairStatus = booking.getRepairStatus() != null ? booking.getRepairStatus().name() : null;
            }
        }

        List<ShopCustomerSummaryResponse> response = new ArrayList<>();
        for (ShopCustomerAccumulator acc : grouped.values()) {
            response.add(new ShopCustomerSummaryResponse(
                    acc.customerUserId,
                    acc.customerName,
                    acc.customerPhone,
                    acc.customerEmail,
                    acc.totalRepairs,
                    acc.totalSpent,
                    acc.lastVisit,
                    acc.latestRepairStatus
            ));
        }

        response.sort(Comparator.comparing(ShopCustomerSummaryResponse::getLastVisit,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return response;
    }

    private String buildCustomerKey(Booking booking) {
        if (booking.getCustomerUserId() != null) return "user:" + booking.getCustomerUserId();
        if (booking.getCustomerPhone() != null && !booking.getCustomerPhone().isBlank()) return "phone:" + booking.getCustomerPhone().trim();
        if (booking.getCustomerEmail() != null && !booking.getCustomerEmail().isBlank()) return "email:" + booking.getCustomerEmail().trim().toLowerCase();
        return "guest:" + safe(booking.getCustomerName()) + ":" + (booking.getCreatedAt() != null ? booking.getCreatedAt().atOffset(ZoneOffset.UTC) : "na");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class ShopCustomerAccumulator {
        Long customerUserId;
        String customerName;
        String customerPhone;
        String customerEmail;
        long totalRepairs;
        BigDecimal totalSpent = BigDecimal.ZERO;
        Instant lastVisit;
        String latestRepairStatus;
    }
}
