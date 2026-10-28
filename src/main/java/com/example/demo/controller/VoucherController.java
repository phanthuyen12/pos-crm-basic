package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.VoucherApplyResult;
import com.example.demo.dto.VoucherDto;
import com.example.demo.dto.VoucherRequest;
import com.example.demo.dto.ApplyVoucherRequest;
import com.example.demo.service.VoucherService;
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
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ApiResponse<List<VoucherDto>> list() {
        return ApiResponse.ok(voucherService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<VoucherDto> get(@PathVariable Long id) {
        return ApiResponse.ok(voucherService.findById(id));
    }

    @PostMapping("/apply")
    public ApiResponse<VoucherApplyResult> apply(@RequestBody ApplyVoucherRequest request) {
        return ApiResponse.ok(voucherService.apply(request.getCode(), request.getSubtotal()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherDto>> create(@RequestBody VoucherRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo voucher thành công", voucherService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<VoucherDto> update(@PathVariable Long id, @RequestBody VoucherRequest request) {
        return ApiResponse.ok("Cập nhật voucher thành công", voucherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ApiResponse.ok("Xóa voucher thành công", null);
    }
}