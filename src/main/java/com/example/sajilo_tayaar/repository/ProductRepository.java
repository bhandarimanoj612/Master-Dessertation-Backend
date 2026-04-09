package com.example.sajilo_tayaar.repository;

import com.example.sajilo_tayaar.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);
    Optional<Product> findByIdAndShopId(Long id, Long shopId);
    boolean existsByShopIdAndSkuIgnoreCase(Long shopId, String sku);
    boolean existsByShopIdAndSkuIgnoreCaseAndIdNot(Long shopId, String sku, Long id);
}
