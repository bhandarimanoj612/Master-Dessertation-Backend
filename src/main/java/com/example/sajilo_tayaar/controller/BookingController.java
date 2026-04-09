package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.*;
import com.example.sajilo_tayaar.dto.response.BookingDetailsResponse;
import com.example.sajilo_tayaar.dto.response.BookingResponse;
import com.example.sajilo_tayaar.dto.response.TimelineItemResponse;
import com.example.sajilo_tayaar.entity.BookingStatusHistory;
import com.example.sajilo_tayaar.services.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    // These endpoints are used by both the customer flow and the shop-side repair flow.

    private final BookingService bookingService;

    @PostMapping
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest req) {
        return bookingService.create(req);
    }

    @PatchMapping("/{bookingId}/estimate")
    public BookingResponse provideEstimate(
            @PathVariable Long bookingId,
            @Valid @RequestBody ProvideEstimateRequest req
    ) {
        return bookingService.provideEstimate(
                bookingId,
                req.estimatedPrice(),
                req.technicianNote(),
                req.changedBy()
        );
    }

    @PatchMapping("/{bookingId}/confirm-estimate")
    public BookingResponse confirmEstimate(
            @PathVariable Long bookingId,
            @Valid @RequestBody ConfirmEstimateRequest req
    ) {
        return bookingService.confirmEstimate(
                bookingId,
                req.userId(),
                req.changedBy()
        );
    }

    @PatchMapping("/{bookingId}/status")
    public BookingResponse updateStatus(
            @PathVariable Long bookingId,
            @Valid @RequestBody UpdateStatusRequest req
    ) {
        return bookingService.updateStatus(
                bookingId,
                req.status(),
                req.changedBy(),
                req.remarks()
        );
    }

    @PatchMapping("/admin/{bookingId}/assign-technician")
    public BookingResponse assignTechnician(
            @PathVariable Long bookingId,
            @Valid @RequestBody AssignTechnicianRequest req
    ) {
        return bookingService.assignTechnician(
                bookingId,
                req.technicianId(),
                req.changedBy(),
                req.remarks()
        );
    }

    @PatchMapping("/{bookingId}/pay")
    public BookingResponse pay(
            @PathVariable Long bookingId,
            @Valid @RequestBody PaymentRequest req
    ) {
        return bookingService.markAsPaid(
                bookingId,
                req.finalPrice(),
                req.paymentMethod(),
                req.changedBy()
        );
    }

    @PatchMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody CancelBookingRequest req
    ) {
        return bookingService.cancelByCustomer(
                bookingId,
                req.userId(),
                req.changedBy(),
                req.remarks()
        );
    }

    @PatchMapping("/admin/{bookingId}/cancel")
    public BookingResponse adminCancelBooking(
            @PathVariable Long bookingId,
            @RequestBody AdminCancelBookingRequest req
    ) {
        return bookingService.cancelByAdmin(
                bookingId,
                req.changedBy(),
                req.remarks()
        );
    }

    @GetMapping("/{bookingId}/timeline")
    public List<TimelineItemResponse> timeline(@PathVariable Long bookingId) {
        List<BookingStatusHistory> history = bookingService.getTimeline(bookingId);

        return history.stream()
                .map(h -> new TimelineItemResponse(
                        h.getStatus(),
                        h.getChangedBy(),
                        h.getRemarks(),
                        h.getChangedAt()
                ))
                .toList();
    }

    @GetMapping("/shop/{shopId}")
    public List<BookingDetailsResponse> listForShop(@PathVariable Long shopId) {
        return bookingService.listForShop(shopId);
    }

    @GetMapping("/admin/shop/{shopId}")
    public List<BookingDetailsResponse> adminListForShop(@PathVariable Long shopId) {
        return bookingService.listForShop(shopId);
    }

    @GetMapping("/mine")
    public List<BookingDetailsResponse> myBookings(@RequestParam Long userId) {
        return bookingService.listForCustomerUserId(userId);
    }

    @GetMapping("/mine/{bookingId}")
    public BookingDetailsResponse myBookingDetails(
            @PathVariable Long bookingId,
            @RequestParam Long userId
    ) {
        return bookingService.getMineById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDetailsResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }
}