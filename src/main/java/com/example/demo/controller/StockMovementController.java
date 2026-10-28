package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.StockMovementDto;
import com.example.demo.dto.StockMovementRequest;
import com.example.demo.service.StockMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public ApiResponse<List<StockMovementDto>> list(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ApiResponse.ok(stockMovementService.findByProduct(productId));
        }
        return ApiResponse.ok(stockMovementService.findAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockMovementDto>> create(@RequestBody StockMovementRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo phiếu nhập/xuất thành công", stockMovementService.create(request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        stockMovementService.delete(id);
        return ApiResponse.ok("Xóa phiếu nhập/xuất thành công", null);
    }
}