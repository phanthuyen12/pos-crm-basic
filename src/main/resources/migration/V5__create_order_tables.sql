-- customer_id / employee_id both reference `people` (type = 'CUSTOMER' /
-- 'EMPLOYEE' respectively), enforced at the application layer.
CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(20) NOT NULL,
    customer_id BIGINT UNSIGNED NULL,
    employee_id BIGINT UNSIGNED NULL,
    shift_assignment_id BIGINT UNSIGNED NULL,
    voucher_id BIGINT UNSIGNED NULL,
    order_type ENUM('DINE_IN','TAKEAWAY','DELIVERY') NOT NULL DEFAULT 'TAKEAWAY',
    status ENUM('PENDING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    payment_method ENUM('CASH','CARD','QR') NULL,
    subtotal DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    note VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at DATETIME NULL,
    CONSTRAINT uk_orders_code UNIQUE (order_code),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES people(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_employee FOREIGN KEY (employee_id) REFERENCES people(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_shift_assignment FOREIGN KEY (shift_assignment_id) REFERENCES shift_assignments(id) ON DELETE SET NULL,
    CONSTRAINT fk_orders_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Order detail / line items ("chi tiết đơn hàng").
CREATE TABLE order_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NULL,
    product_name VARCHAR(150) NOT NULL,
    unit_price DECIMAL(14,2) NOT NULL,
    quantity INT UNSIGNED NOT NULL DEFAULT 1,
    note VARCHAR(255) NULL,
    line_total DECIMAL(14,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_items_order ON order_items(order_id);
