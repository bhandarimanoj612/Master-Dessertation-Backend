package com.example.sajilo_tayaar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private long totalBookings;
    private long requestedCount;
    private long estimateProvidedCount;
    private long customerConfirmedCount;
    private long confirmedCount;
    private long inProgressCount;
    private long completedCount;
    private long paidCount;
    private long cancelledCount;
    private long totalCustomers;
    private long totalProducts;
    private long lowStockProducts;
    private long totalInvoices;
    private BigDecimal totalRevenue;
}
