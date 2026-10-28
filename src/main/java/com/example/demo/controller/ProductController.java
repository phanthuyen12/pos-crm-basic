package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ProductDto;
import com.example.demo.dto.ProductRequest;
import com.example.demo.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<ProductDto>> list(@RequestParam(required = false) Long categoryId,
                                              @RequestParam(defaultValue = "false") boolean activeOnly) {
        if (categoryId != null) {
            return ApiResponse.ok(productService.findByCategory(categoryId));
        }
        if (activeOnly) {
            return ApiResponse.ok(productService.findActive());
        }
        return ApiResponse.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> create(@RequestBody ProductRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo sản phẩm thành công", productService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ApiResponse.ok("Cập nhật sản phẩm thành công", productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok("Xóa sản phẩm thành công", null);
    }
}