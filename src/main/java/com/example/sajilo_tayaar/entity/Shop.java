package com.example.sajilo_tayaar.entity;

import com.example.sajilo_tayaar.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "shops")
@Getter
@Setter
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String streetAddress;

    private String area;
    private String city;
    private String state;
    private String postalCode;
    private String phone;

    @Column(nullable = false)
    private Double avgRating = 0.0;

    @Column(nullable = false)
    private Integer ratingCount = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Double lat;

    @Column
    private Double lng;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.LISTED;

    @Column
    private Instant verificationRequestedAt;

    @Column
    private Instant verifiedAt;

    @Column(length = 1000)
    private String verificationNotes;

}
