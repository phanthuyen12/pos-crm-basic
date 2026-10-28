package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderRequest;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<OrderDto>> list(@RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            return ApiResponse.ok(orderService.findByStatus(status));
        }
        return ApiResponse.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDto> get(@PathVariable Long id) {
        return ApiResponse.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> create(@RequestBody OrderRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Tạo đơn hàng thành công", orderService.create(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderDto> update(@PathVariable Long id, @RequestBody OrderRequest request) {
        return ApiResponse.ok("Cập nhật đơn hàng thành công", orderService.update(id, request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderDto> pay(@PathVariable Long id, @RequestBody PayRequest request) {
        return ApiResponse.ok("Thanh toán thành công", orderService.pay(id, request.getPaymentMethod()));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderDto> cancel(@PathVariable Long id) {
        return ApiResponse.ok("Đã hủy đơn hàng", orderService.cancel(id));
    }

    public static class PayRequest {
        private PaymentMethod paymentMethod;

        public PaymentMethod getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }
}
