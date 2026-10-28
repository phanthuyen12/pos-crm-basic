-- Add login credentials for employees. Frontend sends plaintext for this
-- demo; store simple value to keep the student-style project self-contained.
ALTER TABLE people ADD COLUMN password VARCHAR(255) NULL;

UPDATE people
SET password = '123456'
WHERE type = 'EMPLOYEE';

-- Extra employees so roles have accounts to log in with.
INSERT INTO people (type, full_name, phone, employee_code, role, hire_date, status, password) VALUES
    ('EMPLOYEE', 'Trần Thị Bích', '0909888998', 'NV002', 'CASHIER', '2024-05-15', 'ACTIVE', '123456'),
    ('EMPLOYEE', 'Lê Văn Cường', '0912345678', 'NV003', 'WAITER', '2024-08-02', 'ACTIVE', '123456'),
    ('EMPLOYEE', 'Phạm Thị Duyên', '0988112233', 'NV004', 'KITCHEN', '2025-01-20', 'ACTIVE', '123456');
