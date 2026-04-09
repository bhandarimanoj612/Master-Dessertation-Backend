package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    // correct for @ManyToOne Shop shop;
    List<Technician> findByShop_IdAndIsActiveTrue(Long shopId);

    // (optional) get all technicians for a shop (active + inactive)
    List<Technician> findByShop_Id(Long shopId);

    @Modifying
    @Transactional
    void deleteByShop(Shop shop);

    @Modifying
    @Transactional
    @Query("DELETE FROM Technician t WHERE t.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}