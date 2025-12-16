-- Категории
INSERT INTO category (name) VALUES ('Roses');
INSERT INTO category (name) VALUES ('Spring Flowers');
INSERT INTO category (name) VALUES ('Bouquets');
INSERT INTO category (name) VALUES ('Exotic');

-- Цветы (product)
INSERT INTO product (name, description, price, in_stock, availability, category_id)
VALUES ('Red Roses Bouquet', 'A classic bouquet...', 1200.00, 50, true, 1);

INSERT INTO product (name, description, price, in_stock, availability, category_id)
VALUES ('White Avalanche Roses', 'Elegant white roses...', 150.00, 100, true, 1);