package com.example.demo.service;

import com.example.demo.dto.StockMovementDto;
import com.example.demo.dto.StockMovementRequest;
import com.example.demo.entity.InventoryStockMovement;
import com.example.demo.entity.Person;
import com.example.demo.entity.Product;
import com.example.demo.entity.StockMovementType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.InventoryStockMovementRepository;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StockMovementService {

    private final InventoryStockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final PersonRepository personRepository;

    public StockMovementService(InventoryStockMovementRepository movementRepository,
                                ProductRepository productRepository,
                                PersonRepository personRepository) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> findAll() {
        return movementRepository.findAll().stream().map(StockMovementDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> findByProduct(Long productId) {
        return movementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(StockMovementDto::from).toList();
    }

    public StockMovementDto create(StockMovementRequest request) {
        if (request.getProductId() == null) {
            throw ApiException.badRequest("productId không được để trống");
        }
        if (request.getQuantity() == null || request.getQuantity() == 0) {
            throw ApiException.badRequest("Số lượng phải khác 0");
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm id=" + request.getProductId()));

        StockMovementType type = request.getMovementType() != null ? request.getMovementType() : StockMovementType.IN;
        int delta = request.getQuantity();
        if (type == StockMovementType.OUT) {
            delta = -Math.abs(request.getQuantity());
        }
        int newStock = product.getStockQuantity() + delta;
        if (newStock < 0) {
            throw ApiException.badRequest("Tồn kho không đủ để xuất (hiện còn " + product.getStockQuantity() + ")");
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);

        InventoryStockMovement movement = new InventoryStockMovement();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(Math.abs(request.getQuantity()));
        movement.setSupplier(request.getSupplier());
        movement.setNote(request.getNote());
        if (request.getCreatedById() != null) {
            Person person = personRepository.findById(request.getCreatedById())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy nhân viên id=" + request.getCreatedById()));
            movement.setCreatedBy(person);
        }
        return StockMovementDto.from(movementRepository.save(movement));
    }

    public void delete(Long id) {
        movementRepository.delete(
                movementRepository.findById(id)
                        .orElseThrow(() -> ApiException.notFound("Không tìm thấy phiếu nhập/xuất id=" + id))
        );
    }
}