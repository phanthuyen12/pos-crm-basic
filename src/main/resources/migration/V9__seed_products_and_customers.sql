INSERT INTO products (category_id, name, price, stock_quantity, low_stock_threshold, status, icon) VALUES
    ((SELECT id FROM product_categories WHERE slug='drink'), 'Cà phê sữa đá', 25000, 100, 10, 'ACTIVE', 'bi-cup-hot'),
    ((SELECT id FROM product_categories WHERE slug='drink'), 'Cà phê đen đá', 20000, 100, 10, 'ACTIVE', 'bi-cup-hot'),
    ((SELECT id FROM product_categories WHERE slug='drink'), 'Trà sữa trân châu', 35000, 80, 10, 'ACTIVE', 'bi-cup-straw'),
    ((SELECT id FROM product_categories WHERE slug='drink'), 'Sinh tố bơ', 30000, 60, 10, 'ACTIVE', 'bi-cup'),
    ((SELECT id FROM product_categories WHERE slug='drink'), 'Nước ép cam', 28000, 60, 10, 'ACTIVE', 'bi-cup'),
    ((SELECT id FROM product_categories WHERE slug='food'), 'Bánh mì trứng', 20000, 50, 10, 'ACTIVE', 'bi-egg-fried'),
    ((SELECT id FROM product_categories WHERE slug='food'), 'Bánh mì thịt', 25000, 50, 10, 'ACTIVE', 'bi-egg-fried'),
    ((SELECT id FROM product_categories WHERE slug='food'), 'Cơm gà xối mỡ', 45000, 40, 10, 'ACTIVE', 'bi-egg-fried'),
    ((SELECT id FROM product_categories WHERE slug='food'), 'Mì trộn', 35000, 40, 10, 'ACTIVE', 'bi-egg-fried'),
    ((SELECT id FROM product_categories WHERE slug='dessert'), 'Bánh flan', 15000, 70, 10, 'ACTIVE', 'bi-cake2'),
    ((SELECT id FROM product_categories WHERE slug='dessert'), 'Chè khúc bạch', 18000, 70, 10, 'ACTIVE', 'bi-cake2'),
    ((SELECT id FROM product_categories WHERE slug='dessert'), 'Kem ốc quế', 12000, 50, 10, 'ACTIVE', 'bi-cake2'),
    ((SELECT id FROM product_categories WHERE slug='snack'), 'Khoai tây chiên', 22000, 60, 10, 'ACTIVE', 'bi-basket2'),
    ((SELECT id FROM product_categories WHERE slug='snack'), 'Gà rán', 38000, 50, 10, 'ACTIVE', 'bi-basket2'),
    ((SELECT id FROM product_categories WHERE slug='snack'), 'Nem chua rán', 20000, 50, 10, 'ACTIVE', 'bi-basket2'),
    ((SELECT id FROM product_categories WHERE slug='snack'), 'Xúc xích nướng', 15000, 50, 10, 'ACTIVE', 'bi-basket2');

INSERT INTO people (type, full_name, phone, status) VALUES
    ('CUSTOMER', 'Nguyễn Văn A', '0901234568', 'ACTIVE'),
    ('CUSTOMER', 'Trần Thị B', '0909888999', 'ACTIVE');
