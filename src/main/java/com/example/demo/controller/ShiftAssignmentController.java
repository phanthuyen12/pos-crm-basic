package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ShiftAssignmentDto;
import com.example.demo.dto.ShiftAssignmentRequest;
import com.example.demo.service.ShiftAssignmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shift-assignments")
public class ShiftAssignmentController {

    private final ShiftAssignmentService assignmentService;

    public ShiftAssignmentController(ShiftAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public ApiResponse<List<ShiftAssignmentDto>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        if (workDate != null) {
            return ApiResponse.ok(assignmentService.findByDate(workDate));
        }
        return ApiResponse.ok(assignmentService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ShiftAssignmentDto> get(@PathVariable Long id) {
        return ApiResponse.ok(assignmentService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShiftAssignmentDto>> create(@RequestBody ShiftAssignmentRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Xếp ca thành công", assignmentService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ShiftAssignmentDto> update(@PathVariable Long id, @RequestBody ShiftAssignmentRequest request) {
        return ApiResponse.ok("Cập nhật phân ca thành công", assignmentService.update(id, request));
    }

    @PostMapping("/{id}/check-in")
    public ApiResponse<ShiftAssignmentDto> checkIn(@PathVariable Long id) {
        return ApiResponse.ok("Đã chấm công vào ca", assignmentService.checkIn(id));
    }

    @PostMapping("/{id}/check-out")
    public ApiResponse<ShiftAssignmentDto> checkOut(@PathVariable Long id) {
        return ApiResponse.ok("Đã chấm công tan ca", assignmentService.checkOut(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ApiResponse.ok("Xóa phân ca thành công", null);
    }
}