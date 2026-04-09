package com.example.sajilo_tayaar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // tenant owner
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Shop shop;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 80)
    private String sku; // optional unique per shop (we’ll enforce later)

    @Column(length = 80)
    private String category; // e.g. Mobile Parts, Accessories

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private Integer stockQty = 0;

    @Column(nullable = false)
    private Boolean active = true;

    private Instant createdAt = Instant.now();
}
