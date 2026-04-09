package com.example.sajilo_tayaar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShopCustomerSummaryResponse {
    private Long customerUserId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private long totalRepairs;
    private BigDecimal totalSpent;
    private Instant lastVisit;
    private String latestRepairStatus;
}
