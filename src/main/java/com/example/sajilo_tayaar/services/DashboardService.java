package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.response.DashboardSummaryResponse;
import com.example.sajilo_tayaar.entity.Booking;
import com.example.sajilo_tayaar.entity.Product;
import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import com.example.sajilo_tayaar.repository.BookingRepository;
import com.example.sajilo_tayaar.repository.InvoiceRepository;
import com.example.sajilo_tayaar.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardSummaryResponse getSummary(Long shopId) {
        List<Booking> bookings = bookingRepository.findByShopId(shopId);
        List<Product> products = productRepository.findByShopId(shopId);

        long totalBookings = bookings.size();

        long requestedCount = 0;
        long estimateProvidedCount = 0;
        long customerConfirmedCount = 0;
        long confirmedCount = 0;
        long inProgressCount = 0;
        long completedCount = 0;
        long paidCount = 0;
        long cancelledCount = 0;

        Set<String> uniqueCustomers = new HashSet<>();

        for (Booking booking : bookings) {
            if (booking.getCustomerUserId() != null) {
                uniqueCustomers.add("u:" + booking.getCustomerUserId());
            } else if (booking.getCustomerPhone() != null && !booking.getCustomerPhone().isBlank()) {
                uniqueCustomers.add("p:" + booking.getCustomerPhone().trim());
            } else if (booking.getCustomerEmail() != null && !booking.getCustomerEmail().isBlank()) {
                uniqueCustomers.add("e:" + booking.getCustomerEmail().trim().toLowerCase());
            } else if (booking.getCustomerName() != null && !booking.getCustomerName().isBlank()) {
                uniqueCustomers.add("n:" + booking.getCustomerName().trim().toLowerCase());
            }

            RepairStatus status = booking.getRepairStatus();
            if (status == null) {
                continue;
            }

            switch (status) {
                case REQUESTED -> requestedCount++;
                case ESTIMATE_PROVIDED -> estimateProvidedCount++;
                case CUSTOMER_CONFIRMED -> customerConfirmedCount++;
                case CONFIRMED -> confirmedCount++;
                case DIAGNOSIS,
                     IN_PROGRESS,
                     WAITING_PARTS,
                     PICKUP_SCHEDULED,
                     PICKED_UP,
                     DROPPED_OFF,
                     RECEIVED_AT_SHOP,
                     READY,
                     DELIVERING,
                     DELIVERED -> inProgressCount++;
                case COMPLETED -> completedCount++;
                case PAID -> paidCount++;
                case CANCELLED -> cancelledCount++;
            }
        }

        long lowStockProducts = products.stream()
                .filter(product -> product.getStockQty() != null && product.getStockQty() <= 5)
                .count();

        BigDecimal totalRevenue = bookings.stream()
                .filter(booking -> Boolean.TRUE.equals(booking.getPaid()))
                .map(booking -> booking.getFinalPrice() == null ? BigDecimal.ZERO : booking.getFinalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalInvoices = invoiceRepository.findByShopIdOrderByIdDesc(shopId).size();

        return new DashboardSummaryResponse(
                totalBookings,
                requestedCount,
                estimateProvidedCount,
                customerConfirmedCount,
                confirmedCount,
                inProgressCount,
                completedCount,
                paidCount,
                cancelledCount,
                uniqueCustomers.size(),
                products.size(),
                lowStockProducts,
                totalInvoices,
                totalRevenue
        );
    }
}