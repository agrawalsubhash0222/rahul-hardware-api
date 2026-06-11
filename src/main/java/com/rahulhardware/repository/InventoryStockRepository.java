package com.rahulhardware.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rahulhardware.entity.InventoryStock;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    List<InventoryStock> findByActiveTrueOrderByProductNameAsc();

    List<InventoryStock> findByCurrentStockLessThanAndActiveTrue(Integer limit);

    List<InventoryStock> findByCurrentStockAndActiveTrue(Integer stock);

    List<InventoryStock> findByProductNameContainingIgnoreCaseAndActiveTrue(String productName);

    boolean existsByProductId(Long productId);
}