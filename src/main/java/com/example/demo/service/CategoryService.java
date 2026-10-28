package com.example.demo.service;

import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.CategoryRequest;
import com.example.demo.entity.ProductCategory;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;

    public CategoryService(ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(CategoryDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return CategoryDto.from(getEntity(id));
    }

    public CategoryDto create(CategoryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw ApiException.badRequest("Tên danh mục không được để trống");
        }
        if (request.getSlug() == null || request.getSlug().isBlank()) {
            throw ApiException.badRequest("Slug không được để trống");
        }
        if (categoryRepository.findBySlug(request.getSlug()).isPresent()) {
            throw ApiException.badRequest("Slug đã tồn tại");
        }
        ProductCategory category = new ProductCategory();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        return CategoryDto.from(categoryRepository.save(category));
    }

    public CategoryDto update(Long id, CategoryRequest request) {
        ProductCategory category = getEntity(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            category.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            category.setSlug(request.getSlug());
        }
        return CategoryDto.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.delete(getEntity(id));
    }

    private ProductCategory getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy danh mục id=" + id));
    }
}