package com.example.demo.service;

import com.example.demo.dto.ProductDto;
import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductCategory;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.ProductCategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAll() {
        return productRepository.findAll().stream().map(ProductDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findActive() {
        return productRepository.findByStatus(ActiveStatus.ACTIVE).stream().map(ProductDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream().map(ProductDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return ProductDto.from(getEntity(id));
    }

    public ProductDto create(ProductRequest request) {
        validate(request);
        Product product = new Product();
        applyFields(product, request);
        return ProductDto.from(productRepository.save(product));
    }

    public ProductDto update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        validate(request);
        applyFields(product, request);
        return ProductDto.from(productRepository.save(product));
    }

    public void delete(Long id) {
        productRepository.delete(getEntity(id));
    }

    private void validate(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw ApiException.badRequest("Tên sản phẩm không được để trống");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest("Giá sản phẩm không hợp lệ");
        }
    }

    private void applyFields(Product product, ProductRequest request) {
        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy danh mục id=" + request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);
        product.setStatus(request.getStatus() != null ? request.getStatus() : ActiveStatus.ACTIVE);
        product.setIcon(request.getIcon());
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm id=" + id));
    }
}