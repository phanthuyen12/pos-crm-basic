package com.example.demo.dto;

import com.example.demo.entity.InventoryStockMovement;
import com.example.demo.entity.StockMovementType;

import java.time.LocalDateTime;

public class StockMovementDto {

    private Long id;
    private Long productId;
    private String productName;
    private StockMovementType movementType;
    private Integer quantity;
    private String supplier;
    private String note;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;

    public static StockMovementDto from(InventoryStockMovement m) {
        StockMovementDto dto = new StockMovementDto();
        dto.setId(m.getId());
        if (m.getProduct() != null) {
            dto.setProductId(m.getProduct().getId());
            dto.setProductName(m.getProduct().getName());
        }
        dto.setMovementType(m.getMovementType());
        dto.setQuantity(m.getQuantity());
        dto.setSupplier(m.getSupplier());
        dto.setNote(m.getNote());
        if (m.getCreatedBy() != null) {
            dto.setCreatedById(m.getCreatedBy().getId());
            dto.setCreatedByName(m.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(StockMovementType movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}