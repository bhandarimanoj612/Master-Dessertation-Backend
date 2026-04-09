package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findByShop_Id(Long tenantId, Pageable pageable);
    long countByShop_Id(Long tenantId);

    @Modifying
    @Transactional
    void deleteByShop(Shop shop);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") Long shopId);
}