package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.CategoryRequest;
import com.example.demo.service.CategoryService;
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
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> list() {
        return ApiResponse.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryDto> get(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> create(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo danh mục thành công", categoryService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDto> update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return ApiResponse.ok("Cập nhật danh mục thành công", categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok("Xóa danh mục thành công", null);
    }
}