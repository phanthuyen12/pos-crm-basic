package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ShiftDto;
import com.example.demo.dto.ShiftRequest;
import com.example.demo.service.ShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ApiResponse<List<ShiftDto>> list() {
        return ApiResponse.ok(shiftService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShiftDto> get(@PathVariable Long id) {
        return ApiResponse.ok(shiftService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShiftDto>> create(@RequestBody ShiftRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo ca làm việc thành công", shiftService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShiftDto> update(@PathVariable Long id, @RequestBody ShiftRequest request) {
        return ApiResponse.ok("Cập nhật ca làm việc thành công", shiftService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ApiResponse.ok("Xóa ca làm việc thành công", null);
    }
}