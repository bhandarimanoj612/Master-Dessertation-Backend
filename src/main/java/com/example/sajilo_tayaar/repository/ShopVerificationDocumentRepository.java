package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.ShopVerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ShopVerificationDocumentRepository extends JpaRepository<ShopVerificationDocument, Long> {
    List<ShopVerificationDocument> findByShopId(Long shopId);

    @Modifying
    @Transactional
    void deleteByShop(Shop shop);

    @Modifying
    @Transactional
    @Query("DELETE FROM ShopVerificationDocument svd WHERE svd.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}