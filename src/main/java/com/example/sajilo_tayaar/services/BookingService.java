package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.request.CreateBookingRequest;
import com.example.sajilo_tayaar.dto.response.BookingDetailsResponse;
import com.example.sajilo_tayaar.dto.response.BookingResponse;
import com.example.sajilo_tayaar.dto.response.ShopSummaryResponse;
import com.example.sajilo_tayaar.dto.response.TechnicianSummaryResponse;
import com.example.sajilo_tayaar.entity.Booking;
import com.example.sajilo_tayaar.entity.BookingStatusHistory;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.Technician;
import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import com.example.sajilo_tayaar.entity.enums.ServiceMode;
import com.example.sajilo_tayaar.repository.BookingRepository;
import com.example.sajilo_tayaar.repository.BookingStatusHistoryRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    // I kept all booking logic here so the flow is easier to follow in one place.

    private final BookingRepository bookingRepository;
    private final ShopRepository shopRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final TechnicianRepository technicianRepository;

    // Step 1: customer sends a repair request from the frontend form.
    public BookingResponse create(CreateBookingRequest req) {
        Shop shop = shopRepository.findById(req.shopId())
                .orElseThrow(() -> new IllegalArgumentException("Shop not found"));

        validatePickupAddress(req.serviceMode(), req.pickupAddress());

        Booking booking = new Booking();
        booking.setShop(shop);
        booking.setCustomerUserId(req.customerUserId());
        booking.setCustomerName(trim(req.customerName()));
        booking.setCustomerPhone(trim(req.customerPhone()));
        booking.setCustomerEmail(trimToNull(req.customerEmail()));
        booking.setBookingType(req.bookingType());
        booking.setServiceMode(req.serviceMode());
        booking.setDeviceCategory(trim(req.deviceCategory()));
        booking.setDeviceModel(trimToNull(req.deviceModel()));
        booking.setIssueDescription(trim(req.issueDescription()));
        booking.setPickupAddress(trimToNull(req.pickupAddress()));
        booking.setPreferredDateTime(req.preferredDateTime());

        Booking saved = bookingRepository.save(booking);

        saveHistory(saved, RepairStatus.REQUESTED, "SYSTEM", "Repair request created");

        return toBookingResponse(saved);
    }

    // Step 2: shop/technician sends an estimated price and note.
    public BookingResponse provideEstimate(Long bookingId, BigDecimal estimatedPrice, String technicianNote, String changedBy) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getRepairStatus() != RepairStatus.REQUESTED) {
            throw new IllegalArgumentException("Estimate can only be provided for REQUESTED bookings");
        }

        if (estimatedPrice == null || estimatedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Estimated price must be greater than zero");
        }

        booking.setEstimatedPrice(estimatedPrice);
        booking.setTechnicianNote(trimToNull(technicianNote));
        booking.setRepairStatus(RepairStatus.ESTIMATE_PROVIDED);

        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                RepairStatus.ESTIMATE_PROVIDED,
                defaultActor(changedBy, "TECHNICIAN"),
                "Estimate provided"
        );

        return toBookingResponse(saved);
    }

    // Step 3: customer accepts the estimate before the shop starts the work.
    public BookingResponse confirmEstimate(Long bookingId, Long userId, String changedBy) {
        Booking booking = bookingRepository.findByIdAndCustomerUserId(bookingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getRepairStatus() != RepairStatus.ESTIMATE_PROVIDED) {
            throw new IllegalArgumentException("Booking is not waiting for customer confirmation");
        }

        booking.setCustomerApprovedEstimate(true);
        booking.setRepairStatus(RepairStatus.CUSTOMER_CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                RepairStatus.CUSTOMER_CONFIRMED,
                defaultActor(changedBy, "CUSTOMER"),
                "Customer confirmed estimate"
        );

        return toBookingResponse(saved);
    }

    // I check status changes here so users cannot jump to the wrong repair step.
    public BookingResponse updateStatus(Long bookingId, RepairStatus nextStatus, String changedBy, String remarks) {
        Booking booking = getBookingOrThrow(bookingId);
        RepairStatus currentStatus = booking.getRepairStatus();

        if (currentStatus == RepairStatus.CANCELLED || currentStatus == RepairStatus.PAID) {
            throw new IllegalArgumentException("Booking can no longer be updated");
        }

        if (currentStatus == nextStatus) {
            throw new IllegalArgumentException("Booking is already in status " + nextStatus);
        }

        validateManualStatusTransition(currentStatus, nextStatus);

        booking.setRepairStatus(nextStatus);
        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                nextStatus,
                defaultActor(changedBy, "ADMIN"),
                trimToNull(remarks)
        );

        return toBookingResponse(saved);
    }

    public BookingResponse assignTechnician(Long bookingId, Long technicianId, String changedBy, String remarks) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getRepairStatus() == RepairStatus.CANCELLED ||
                booking.getRepairStatus() == RepairStatus.COMPLETED ||
                booking.getRepairStatus() == RepairStatus.PAID) {
            throw new IllegalArgumentException("Cannot assign technician to this booking");
        }

        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new IllegalArgumentException("Technician not found"));

        if (tech.getShop() == null || booking.getShop() == null ||
                !tech.getShop().getId().equals(booking.getShop().getId())) {
            throw new IllegalArgumentException("Technician does not belong to the same shop");
        }

        booking.setAssignedTechnician(tech);
        Booking saved = bookingRepository.save(booking);

        String finalRemarks = trimToNull(remarks) != null
                ? remarks.trim()
                : "Technician assigned: " + tech.getFullName();

        saveHistory(
                saved,
                saved.getRepairStatus(),
                defaultActor(changedBy, "ADMIN"),
                finalRemarks
        );

        return toBookingResponse(saved);
    }

    // Payment is only accepted after the repair has been completed.
    public BookingResponse markAsPaid(Long bookingId, BigDecimal finalPrice, String paymentMethod, String changedBy) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getRepairStatus() != RepairStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed bookings can be paid");
        }

        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Final price must be greater than zero");
        }

        booking.setFinalPrice(finalPrice);
        booking.setPaid(true);
        booking.setPaymentMethod(trimToNull(paymentMethod));
        booking.setPaidAt(Instant.now());
        booking.setRepairStatus(RepairStatus.PAID);

        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                RepairStatus.PAID,
                defaultActor(changedBy, "CUSTOMER"),
                "Payment completed"
        );

        return toBookingResponse(saved);
    }

    public BookingResponse cancelByCustomer(Long bookingId, Long userId, String changedBy, String remarks) {
        Booking booking = bookingRepository.findByIdAndCustomerUserId(bookingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getRepairStatus() == RepairStatus.COMPLETED || booking.getRepairStatus() == RepairStatus.PAID) {
            throw new IllegalArgumentException("Completed or paid booking cannot be cancelled");
        }

        if (booking.getRepairStatus() == RepairStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setRepairStatus(RepairStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                RepairStatus.CANCELLED,
                defaultActor(changedBy, "CUSTOMER"),
                trimToNull(remarks) != null ? remarks.trim() : "Cancelled by customer"
        );

        return toBookingResponse(saved);
    }


    public BookingResponse cancelByAdmin(Long bookingId, String changedBy, String remarks) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getRepairStatus() == RepairStatus.COMPLETED || booking.getRepairStatus() == RepairStatus.PAID) {
            throw new IllegalArgumentException("Completed or paid booking cannot be cancelled");
        }

        if (booking.getRepairStatus() == RepairStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }

        booking.setRepairStatus(RepairStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        saveHistory(
                saved,
                RepairStatus.CANCELLED,
                defaultActor(changedBy, "ADMIN"),
                trimToNull(remarks) != null ? remarks.trim() : "Cancelled by admin"
        );

        return toBookingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingStatusHistory> getTimeline(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new IllegalArgumentException("Booking not found");
        }
        return historyRepository.findByBookingIdOrderByChangedAtAsc(bookingId);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> listForShop(Long shopId) {
        return bookingRepository.findByShopId(shopId)
                .stream()
                .map(this::toBookingDetailsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> listForCustomerUserId(Long userId) {
        return bookingRepository.findByCustomerUserId(userId)
                .stream()
                .map(this::toBookingDetailsResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingDetailsResponse getMineById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndCustomerUserId(bookingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        return toBookingDetailsResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toBookingDetailsResponse)
                .toList();
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    private void validatePickupAddress(ServiceMode serviceMode, String pickupAddress) {
        if (serviceMode == ServiceMode.HOME_PICKUP &&
                (pickupAddress == null || pickupAddress.isBlank())) {
            throw new IllegalArgumentException("pickupAddress is required for HOME_PICKUP");
        }
    }

    private void validateManualStatusTransition(RepairStatus current, RepairStatus next) {
        boolean valid =
                (current == RepairStatus.CUSTOMER_CONFIRMED && next == RepairStatus.CONFIRMED) ||
                        (current == RepairStatus.CONFIRMED && next == RepairStatus.IN_PROGRESS) ||
                        (current == RepairStatus.IN_PROGRESS && next == RepairStatus.COMPLETED) ||
                        (current == RepairStatus.REQUESTED && next == RepairStatus.CANCELLED) ||
                        (current == RepairStatus.ESTIMATE_PROVIDED && next == RepairStatus.CANCELLED) ||
                        (current == RepairStatus.CUSTOMER_CONFIRMED && next == RepairStatus.CANCELLED) ||
                        (current == RepairStatus.CONFIRMED && next == RepairStatus.CANCELLED) ||
                        (current == RepairStatus.IN_PROGRESS && next == RepairStatus.CANCELLED);

        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to " + next);
        }
    }

    private void saveHistory(Booking booking, RepairStatus status, String changedBy, String remarks) {
        BookingStatusHistory history = new BookingStatusHistory();
        history.setShop(booking.getShop());
        history.setBooking(booking);
        history.setStatus(status);
        history.setChangedBy(changedBy);
        history.setRemarks(remarks);
        historyRepository.save(history);
    }

    private String defaultActor(String changedBy, String fallback) {
        return (changedBy == null || changedBy.isBlank()) ? fallback : changedBy.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private BookingResponse toBookingResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getShop().getId(),
                b.getShop().getName(),
                b.getCustomerName(),
                b.getCustomerPhone(),
                b.getCustomerEmail(),
                b.getBookingType(),
                b.getServiceMode(),
                b.getDeviceCategory(),
                b.getDeviceModel(),
                b.getIssueDescription(),
                b.getPickupAddress(),
                b.getPreferredDateTime(),
                b.getRepairStatus(),
                b.getEstimatedPrice(),
                b.getTechnicianNote(),
                b.getCustomerApprovedEstimate(),
                b.getFinalPrice(),
                b.getPaid(),
                b.getPaymentMethod(),
                b.getPaidAt(),
                b.getCreatedAt()
        );
    }

    private BookingDetailsResponse toBookingDetailsResponse(Booking b) {
        return new BookingDetailsResponse(
                b.getId(),
                new ShopSummaryResponse(
                        b.getShop().getId(),
                        b.getShop().getName(),
                        b.getShop().getStreetAddress(),
                        b.getShop().getArea(),
                        b.getShop().getCity(),
                        b.getShop().getState(),
                        b.getShop().getPostalCode(),
                        b.getShop().getPhone(),
                        b.getShop().getAvgRating(),
                        b.getShop().getRatingCount(),
                        b.getShop().getIsActive(),
                        b.getShop().getCreatedAt(),
                        b.getShop().getLat(),
                        b.getShop().getLng(),
                        b.getShop().getVerified(),
                        b.getShop().getDescription()
                ),
                b.getAssignedTechnician() == null
                        ? null
                        : new TechnicianSummaryResponse(
                        b.getAssignedTechnician().getId(),
                        b.getAssignedTechnician().getFullName(),
                        b.getAssignedTechnician().getPhone(),
                        b.getAssignedTechnician().getSpecialization()
                ),
                b.getCustomerUserId(),
                b.getCustomerName(),
                b.getCustomerPhone(),
                b.getCustomerEmail(),
                b.getBookingType(),
                b.getServiceMode(),
                b.getDeviceCategory(),
                b.getDeviceModel(),
                b.getIssueDescription(),
                b.getPickupAddress(),
                b.getPreferredDateTime(),
                b.getRepairStatus(),
                b.getEstimatedPrice(),
                b.getTechnicianNote(),
                b.getCustomerApprovedEstimate(),
                b.getFinalPrice(),
                b.getPaid(),
                b.getPaymentMethod(),
                b.getPaidAt(),
                b.getCreatedAt()
        );
    }
}