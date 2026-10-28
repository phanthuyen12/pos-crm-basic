package com.example.demo.service;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderItemRequest;
import com.example.demo.dto.OrderRequest;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.OrderType;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.ActiveStatus;
import com.example.demo.entity.Person;
import com.example.demo.entity.PersonType;
import com.example.demo.entity.Product;
import com.example.demo.entity.Voucher;
import com.example.demo.entity.InventoryStockMovement;
import com.example.demo.entity.StockMovementType;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.InventoryStockMovementRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PersonRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final PersonRepository personRepository;
    private final InventoryStockMovementRepository stockMovementRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        VoucherRepository voucherRepository,
                        PersonRepository personRepository,
                        InventoryStockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.personRepository = personRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(OrderDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream().map(OrderDto::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return OrderDto.from(getEntity(id));
    }

    public OrderDto create(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw ApiException.badRequest("Đơn hàng cần ít nhất một sản phẩm");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : OrderType.TAKEAWAY);
        order.setStatus(OrderStatus.PENDING);
        order.setNote(request.getNote());

        if (request.getCustomerId() != null) {
            Person customer = personRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy khách hàng id=" + request.getCustomerId()));
            if (customer.getType() != PersonType.CUSTOMER) {
                throw ApiException.badRequest("person được chọn không phải khách hàng");
            }
            order.setCustomer(customer);
        }
        if (request.getEmployeeId() != null) {
            Person employee = personRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy nhân viên id=" + request.getEmployeeId()));
            if (employee.getType() != PersonType.EMPLOYEE || employee.getStatus() != ActiveStatus.ACTIVE) {
                throw ApiException.badRequest("Nhân viên phụ trách không hợp lệ hoặc đã nghỉ việc");
            }
            order.setEmployee(employee);
        }

        // Build items & subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm id=" + itemReq.getProductId()));
            int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
            if (qty <= 0) {
                throw ApiException.badRequest("Số lượng sản phẩm phải lớn hơn 0");
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            subtotal = subtotal.add(lineTotal);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(qty);
            item.setNote(itemReq.getNote());
            item.setLineTotal(lineTotal);
            order.addItem(item);

        }
        order.setSubtotal(subtotal);

        // Voucher
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy voucher id=" + request.getVoucherId()));
            discount = computeDiscount(voucher, subtotal);
            order.setVoucher(voucher);
        }
        order.setDiscountAmount(discount);

        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(0, RoundingMode.HALF_UP);
        order.setTaxAmount(tax);

        BigDecimal total = taxable.add(tax);
        order.setTotalAmount(total);

        if (request.getPaymentMethod() != null) {
            deductStock(order);
            consumeVoucher(order);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setStatus(OrderStatus.COMPLETED);
            order.setPaidAt(LocalDateTime.now());
        }

        return OrderDto.from(orderRepository.save(order));
    }

    public OrderDto pay(Long id, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw ApiException.badRequest("Phương thức thanh toán không được để trống");
        }
        Order order = getEntity(id);
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw ApiException.badRequest("Đơn hàng đã được thanh toán");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("Đơn hàng đã bị hủy");
        }
        deductStock(order);
        consumeVoucher(order);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaidAt(LocalDateTime.now());
        return OrderDto.from(orderRepository.save(order));
    }

    public OrderDto cancel(Long id) {
        Order order = getEntity(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("Đơn hàng đã bị hủy trước đó");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            restoreStock(order);
            releaseVoucher(order);
        }
        order.setStatus(OrderStatus.CANCELLED);
        return OrderDto.from(orderRepository.save(order));
    }

    /** Chỉ đơn đang chờ thanh toán mới được phép sửa. Kho chưa bị thay đổi. */
    public OrderDto update(Long id, OrderRequest request) {
        Order order = getEntity(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw ApiException.badRequest("Chỉ có thể cập nhật đơn đang chờ thanh toán");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw ApiException.badRequest("Đơn hàng cần ít nhất một sản phẩm");
        }

        order.getItems().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null) {
                throw ApiException.badRequest("productId không được để trống");
            }
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm id=" + itemRequest.getProductId()));
            int quantity = itemRequest.getQuantity() == null ? 1 : itemRequest.getQuantity();
            if (quantity <= 0) {
                throw ApiException.badRequest("Số lượng sản phẩm phải lớn hơn 0");
            }
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setNote(itemRequest.getNote());
            item.setLineTotal(lineTotal);
            order.addItem(item);
        }

        order.setSubtotal(subtotal);
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : order.getOrderType());
        order.setNote(request.getNote());
        if (request.getCustomerId() != null) {
            Person customer = personRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy khách hàng id=" + request.getCustomerId()));
            if (customer.getType() != PersonType.CUSTOMER) {
                throw ApiException.badRequest("person được chọn không phải khách hàng");
            }
            order.setCustomer(customer);
        } else {
            order.setCustomer(null);
        }

        Voucher voucher = null;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy voucher id=" + request.getVoucherId()));
            order.setVoucher(voucher);
        } else {
            order.setVoucher(null);
        }
        BigDecimal discount = voucher == null ? BigDecimal.ZERO : computeDiscount(voucher, subtotal);
        applyTotals(order, subtotal, discount);
        return OrderDto.from(orderRepository.save(order));
    }

    private void applyTotals(Order order, BigDecimal subtotal, BigDecimal discount) {
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(0, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setTaxAmount(tax);
        order.setTotalAmount(taxable.add(tax));
    }

    /** Kiểm tra tổng số lượng theo sản phẩm và trừ kho trong cùng transaction. */
    private void deductStock(Order order) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                quantities.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
            }
        }
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm id=" + entry.getKey()));
            if (product.getStockQuantity() < entry.getValue()) {
                throw ApiException.badRequest("Sản phẩm '" + product.getName() + "' chỉ còn " + product.getStockQuantity() + " trong kho");
            }
            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            saveStockMovement(order, product, StockMovementType.OUT, entry.getValue(), "Bán hàng " + order.getOrderCode());
        }
    }

    private void restoreStock(Order order) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                quantities.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
            }
        }
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey()).orElse(null);
            if (product == null) continue;
            product.setStockQuantity(product.getStockQuantity() + entry.getValue());
            saveStockMovement(order, product, StockMovementType.IN, entry.getValue(), "Hoàn kho do hủy " + order.getOrderCode());
        }
    }

    private void saveStockMovement(Order order, Product product, StockMovementType type, int quantity, String note) {
        InventoryStockMovement movement = new InventoryStockMovement();
        movement.setProduct(product);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setCreatedBy(order.getEmployee());
        movement.setNote(note);
        stockMovementRepository.save(movement);
    }

    private void consumeVoucher(Order order) {
        if (order.getVoucher() == null) return;
        Voucher voucher = order.getVoucher();
        // Revalidate at payment time; a pending order may have been left open
        // beyond the voucher validity period or after the usage limit was hit.
        computeDiscount(voucher, order.getSubtotal());
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }

    private void releaseVoucher(Order order) {
        if (order.getVoucher() == null) return;
        Voucher voucher = order.getVoucher();
        if (voucher.getUsedCount() > 0) {
            voucher.setUsedCount(voucher.getUsedCount() - 1);
            voucherRepository.save(voucher);
        }
    }

    private BigDecimal computeDiscount(Voucher v, BigDecimal subtotal) {
        if (v.getStatus() != ActiveStatus.ACTIVE) {
            throw ApiException.badRequest("Voucher không còn hiệu lực");
        }
        LocalDateTime now = LocalDateTime.now();
        if (v.getStartDate() != null && now.toLocalDate().isBefore(v.getStartDate())) {
            throw ApiException.badRequest("Voucher chưa đến hạn sử dụng");
        }
        if (v.getEndDate() != null && now.toLocalDate().isAfter(v.getEndDate())) {
            throw ApiException.badRequest("Voucher đã hết hạn");
        }
        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            throw ApiException.badRequest("Voucher đã hết lượt sử dụng");
        }
        if (subtotal.compareTo(v.getMinOrderAmount()) < 0) {
            throw ApiException.badRequest("Giá trị đơn tối thiểu để áp dụng voucher là " + v.getMinOrderAmount());
        }
        if (v.getDiscountType() == com.example.demo.entity.VoucherDiscountType.PERCENT) {
            BigDecimal d = subtotal.multiply(v.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (v.getMaxDiscountAmount() != null && d.compareTo(v.getMaxDiscountAmount()) > 0) {
                d = v.getMaxDiscountAmount();
            }
            return d.compareTo(subtotal) > 0 ? subtotal : d;
        }
        BigDecimal d = v.getDiscountValue();
        return d.compareTo(subtotal) > 0 ? subtotal : d;
    }

    private String generateOrderCode() {
        long count = orderRepository.count() + 1;
        return "DH" + String.format("%04d", count);
    }

    private Order getEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng id=" + id));
    }
}
