package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Invoice;
import com.example.sajilo_tayaar.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByShopIdOrderByIdDesc(Long shopId);
    Optional<Invoice> findByIdAndShopId(Long id, Long shopId);

    @Modifying
    @Transactional
    void deleteByShop(Shop shop);

    @Modifying
    @Transactional
    @Query("DELETE FROM Invoice i WHERE i.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}