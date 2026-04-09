package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.enums.VerificationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByName(String name);
    List<Shop> findByIsActiveTrue(Sort sort);
    List<Shop> findByVerificationStatus(VerificationStatus status);

    // optional convenience
    List<Shop> findAll(Sort sort);
}
