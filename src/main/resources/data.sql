-- -- Категории
-- INSERT INTO category (name) VALUES ('Roses');
-- INSERT INTO category (name) VALUES ('Spring Flowers');
-- INSERT INTO category (name) VALUES ('Bouquets');
-- INSERT INTO category (name) VALUES ('Exotic');
--
-- -- Цветы (product)
-- INSERT INTO product (name, description, price, in_stock, availability, category_id)
-- VALUES ('Red Roses Bouquet', 'A classic bouquet...', 1200.00, 50, true, 1);
--
-- INSERT INTO product (name, description, price, in_stock, availability, category_id)
-- VALUES ('White Avalanche Roses', 'Elegant white roses...', 150.00, 100, true, 1);

-- Категории (H2 idempotent)
MERGE INTO category (category_id, name) KEY(category_id) VALUES (1, 'Roses');
MERGE INTO category (category_id, name) KEY(category_id) VALUES (2, 'Spring Flowers');
MERGE INTO category (category_id, name) KEY(category_id) VALUES (3, 'Bouquets');
MERGE INTO category (category_id, name) KEY(category_id) VALUES (4, 'Exotic');

-- Продукты (H2 idempotent)
MERGE INTO product (product_id, name, description, price, in_stock, availability, category_id)
    KEY(product_id)
    VALUES (1, 'Red Roses Bouquet', 'A classic bouquet...', 1200.00, 50, true, 1);

MERGE INTO product (product_id, name, description, price, in_stock, availability, category_id)
    KEY(product_id)
    VALUES (2, 'White Avalanche Roses', 'Elegant white roses...', 150.00, 100, true, 1);
