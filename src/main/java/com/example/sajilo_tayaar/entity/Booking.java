package com.example.sajilo_tayaar.entity;

import com.example.sajilo_tayaar.entity.enums.BookingType;
import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import com.example.sajilo_tayaar.entity.enums.ServiceMode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    // Booking ties together customer details, device details, repair progress, and payment state.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_user_id")
    private Long customerUserId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "assigned_technician_id")
    private Technician assignedTechnician;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String customerPhone;

    @Column(length = 150)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingType bookingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceMode serviceMode;

    @Column(nullable = false, length = 80)
    private String deviceCategory;

    @Column(length = 120)
    private String deviceModel;

    @Column(nullable = false, columnDefinition = "text")
    private String issueDescription;

    @Column(length = 255)
    private String pickupAddress;

    private LocalDateTime preferredDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepairStatus repairStatus = RepairStatus.REQUESTED;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedPrice;

    @Column(length = 500)
    private String technicianNote;

    @Column(nullable = false)
    private Boolean customerApprovedEstimate = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(length = 50)
    private String paymentMethod;

    private Instant paidAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}