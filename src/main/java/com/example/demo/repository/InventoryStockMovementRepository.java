package com.example.demo.repository;

import com.example.demo.entity.InventoryStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryStockMovementRepository extends JpaRepository<InventoryStockMovement, Long> {

    List<InventoryStockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
}