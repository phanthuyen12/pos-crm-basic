-- Unified table for everyone the system knows about: customers who buy and
-- employees who work. `type` tells the two apart; fields only relevant to
-- one side (employee_code, role, hire_date) stay NULL for the other.
CREATE TABLE people (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    type ENUM('CUSTOMER','EMPLOYEE') NOT NULL,
    full_name VARCHAR(150) NULL,
    phone VARCHAR(20) NULL,

    -- Employee-only fields
    employee_code VARCHAR(20) NULL,
    role ENUM('MANAGER','CASHIER','WAITER','KITCHEN') NULL,
    hire_date DATE NULL,

    status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_people_phone UNIQUE (phone),
    CONSTRAINT uk_people_employee_code UNIQUE (employee_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_people_type ON people(type);
