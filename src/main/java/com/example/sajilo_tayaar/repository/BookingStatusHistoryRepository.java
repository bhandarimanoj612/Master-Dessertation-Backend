package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.BookingStatusHistory;
import com.example.sajilo_tayaar.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAsc(Long bookingId);
    @Modifying
    @Transactional
    @Query("DELETE FROM BookingStatusHistory bsh WHERE bsh.booking.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);

    @Modifying
    @Transactional
    void deleteByBooking_Shop(Shop shop);
}