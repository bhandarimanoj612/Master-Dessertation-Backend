package com.example.sajilo_tayaar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "technicians")
@Getter
@Setter
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // tenant owner
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Shop shop;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String specialization; // e.g. Mobile, Laptop, TV

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
