package com.example.sajilo_tayaar;

import com.example.sajilo_tayaar.dto.request.CreateBookingRequest;
import com.example.sajilo_tayaar.dto.response.BookingResponse;
import com.example.sajilo_tayaar.entity.Booking;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.Technician;
import com.example.sajilo_tayaar.entity.enums.BookingType;
import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import com.example.sajilo_tayaar.entity.enums.ServiceMode;
import com.example.sajilo_tayaar.repository.BookingRepository;
import com.example.sajilo_tayaar.repository.BookingStatusHistoryRepository;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.TechnicianRepository;
import com.example.sajilo_tayaar.services.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ShopRepository shopRepository;
    @Mock private BookingStatusHistoryRepository historyRepository;
    @Mock private TechnicianRepository technicianRepository;

    @InjectMocks private BookingService bookingService;

    private Shop shop;
    private Booking booking;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        shop.setId(1L);
        shop.setName("Sajilo Repair Center");

        booking = new Booking();
        booking.setId(10L);
        booking.setShop(shop);
        booking.setCustomerUserId(99L);
        booking.setCustomerName("Manoj");
        booking.setCustomerPhone("9800000000");
        booking.setBookingType(BookingType.APPOINTMENT);
        booking.setServiceMode(ServiceMode.IN_STORE);
        booking.setDeviceCategory("Phone");
        booking.setIssueDescription("Screen broken");
        booking.setRepairStatus(RepairStatus.REQUESTED);
        booking.setCustomerApprovedEstimate(false);
        booking.setPaid(false);
    }

    @Test
    void create_shouldSaveBookingAndTrimFields() {
        CreateBookingRequest req = new CreateBookingRequest(
                1L, 99L, "  Manoj  ", " 9800 ", "  manoj@gmail.com  ",
                BookingType.APPOINTMENT, ServiceMode.IN_STORE,
                "  Phone  ", "  iPhone 13  ", "  Screen broken  ", null,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        BookingResponse response = bookingService.create(req);

        assertEquals("Manoj", response.customerName());
        assertEquals("9800", response.customerPhone());
        assertEquals("manoj@gmail.com", response.customerEmail());
        assertEquals("Phone", response.deviceCategory());
        assertEquals("iPhone 13", response.deviceModel());
        assertEquals(RepairStatus.REQUESTED, response.repairStatus());
        verify(historyRepository).save(any());
    }

    @Test
    void create_shouldRequirePickupAddressForHomePickup() {
        CreateBookingRequest req = new CreateBookingRequest(
                1L, 99L, "Manoj", "9800", null,
                BookingType.DIRECT_REQUEST, ServiceMode.HOME_PICKUP,
                "Laptop", null, "No power", "   ", null
        );

        when(shopRepository.findById(1L)).thenReturn(Optional.of(shop));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(req));

        assertEquals("pickupAddress is required for HOME_PICKUP", ex.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenShopNotFound() {
        CreateBookingRequest req = new CreateBookingRequest(
                1L, 99L, "Manoj", "9800", null,
                BookingType.APPOINTMENT, ServiceMode.IN_STORE,
                "Phone", null, "Broken", null, null
        );

        when(shopRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.create(req));

        assertEquals("Shop not found", ex.getMessage());
    }

    @Test
    void provideEstimate_shouldUpdateStatusAndPrice() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.provideEstimate(10L, new BigDecimal("2500"), "  Replace display  ", "tech1");

        assertEquals(RepairStatus.ESTIMATE_PROVIDED, response.repairStatus());
        assertEquals(new BigDecimal("2500"), response.estimatedPrice());
        assertEquals("Replace display", response.technicianNote());
        verify(historyRepository).save(any());
    }

    @Test
    void provideEstimate_shouldRejectNonRequestedBooking() {
        booking.setRepairStatus(RepairStatus.IN_PROGRESS);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.provideEstimate(10L, new BigDecimal("1000"), "note", "tech"));

        assertEquals("Estimate can only be provided for REQUESTED bookings", ex.getMessage());
    }

    @Test
    void confirmEstimate_shouldApproveAndMoveToCustomerConfirmed() {
        booking.setRepairStatus(RepairStatus.ESTIMATE_PROVIDED);
        when(bookingRepository.findByIdAndCustomerUserId(10L, 99L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.confirmEstimate(10L, 99L, "customer1");

        assertTrue(response.customerApprovedEstimate());
        assertEquals(RepairStatus.CUSTOMER_CONFIRMED, response.repairStatus());
        verify(historyRepository).save(any());
    }

    @Test
    void confirmEstimate_shouldRejectWrongStatus() {
        booking.setRepairStatus(RepairStatus.REQUESTED);
        when(bookingRepository.findByIdAndCustomerUserId(10L, 99L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.confirmEstimate(10L, 99L, "customer1"));

        assertEquals("Booking is not waiting for customer confirmation", ex.getMessage());
    }

    @Test
    void updateStatus_shouldAllowCustomerConfirmedToConfirmed() {
        booking.setRepairStatus(RepairStatus.CUSTOMER_CONFIRMED);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.updateStatus(10L, RepairStatus.CONFIRMED, "owner", "accepted");

        assertEquals(RepairStatus.CONFIRMED, response.repairStatus());
    }

    @Test
    void updateStatus_shouldRejectInvalidManualTransition() {
        booking.setRepairStatus(RepairStatus.REQUESTED);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.updateStatus(10L, RepairStatus.PAID, "owner", "bad"));

        assertEquals("Invalid status transition from REQUESTED to PAID", ex.getMessage());
    }

    @Test
    void assignTechnician_shouldRejectDifferentShopTechnician() {
        Technician tech = new Technician();
        Shop otherShop = new Shop();
        otherShop.setId(2L);
        tech.setId(5L);
        tech.setShop(otherShop);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(technicianRepository.findById(5L)).thenReturn(Optional.of(tech));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.assignTechnician(10L, 5L, "owner", null));

        assertEquals("Technician does not belong to the same shop", ex.getMessage());
    }
}
