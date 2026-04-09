package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Booking;
import com.example.sajilo_tayaar.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByShopId(Long shopId);
        List<Booking> findByCustomerUserId(Long customerUserId);
        Optional<Booking> findByIdAndCustomerUserId(Long bookingId, Long customerUserId);

        @Modifying
        @Transactional
        void deleteByShop(Shop shop);

        @Modifying
        @Transactional
        @Query("DELETE FROM Booking b WHERE b.shop.id = :shopId")
        void deleteByShopId(@Param("shopId") Long shopId);
}