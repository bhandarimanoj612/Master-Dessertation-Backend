package com.example.sajilo_tayaar.entity;

import com.example.sajilo_tayaar.entity.enums.RepairStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "booking_status_history")
@Getter
@Setter
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RepairStatus status;

    @Column(nullable = false, length = 100)
    private String changedBy;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false)
    private Instant changedAt = Instant.now();
}