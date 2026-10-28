-- Reference/seed data matching the demo content already shown in the static UI
-- (admin/shifts.html, admin/products.html). Safe to delete this file if you
-- don't want any pre-filled rows in a fresh database.

INSERT INTO shifts (name, start_time, end_time, required_staff, is_active) VALUES
    ('Ca sáng', '06:00:00', '14:00:00', 3, 1),
    ('Ca chiều', '14:00:00', '22:00:00', 3, 1),
    ('Ca tối', '22:00:00', '06:00:00', 2, 0);

INSERT INTO product_categories (name, slug) VALUES
    ('Đồ uống', 'drink'),
    ('Thức ăn', 'food'),
    ('Tráng miệng', 'dessert'),
    ('Ăn vặt', 'snack');

INSERT INTO people (type, full_name, phone, employee_code, role, hire_date, status) VALUES
    ('EMPLOYEE', 'Nguyễn Văn An', '0901234567', 'NV001', 'MANAGER', '2024-03-01', 'ACTIVE');

INSERT INTO vouchers (code, description, discount_type, discount_value, min_order_amount, status) VALUES
    ('WELCOME10', 'Giảm 10% cho đơn hàng đầu tiên', 'PERCENT', 10, 50000, 'ACTIVE');
