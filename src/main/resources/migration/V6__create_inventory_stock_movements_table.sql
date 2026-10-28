-- created_by references `people` (type = 'employee').
CREATE TABLE inventory_stock_movements (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT UNSIGNED NOT NULL,
    movement_type ENUM('IN','OUT','ADJUSTMENT') NOT NULL DEFAULT 'IN',
    quantity INT NOT NULL,
    supplier VARCHAR(150) NULL,
    note VARCHAR(255) NULL,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_movements_employee FOREIGN KEY (created_by) REFERENCES people(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_stock_movements_product ON inventory_stock_movements(product_id);
