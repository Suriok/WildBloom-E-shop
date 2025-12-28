INSERT INTO category (category_id, name) VALUES (1, 'Roses') ON CONFLICT DO NOTHING;
INSERT INTO category (category_id, name) VALUES (2, 'Spring Flowers') ON CONFLICT DO NOTHING;
INSERT INTO category (category_id, name) VALUES (3, 'Bouquets') ON CONFLICT DO NOTHING;
INSERT INTO category (category_id, name) VALUES (4, 'Exotic') ON CONFLICT DO NOTHING;


SELECT setval('category_id_seq', (SELECT MAX(category_id) FROM category));


INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (1, 'Red Roses Bouquet', 'Classic red roses.', 1200.00, 50, true, 1)
    ON CONFLICT DO NOTHING;

INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (2, 'White Avalanche Roses', 'Elegant white roses.', 150.00, 100, true, 1)
    ON CONFLICT DO NOTHING;

INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (3, 'Pink Roses', 'Gentle pink roses.', 130.00, 80, true, 1)
    ON CONFLICT DO NOTHING;

INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (4, 'Yellow Tulips', 'Bright yellow tulips.', 80.00, 200, true, 2)
    ON CONFLICT DO NOTHING;

INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (5, 'Purple Tulips', 'Deep purple tulips.', 85.00, 150, true, 2)
    ON CONFLICT DO NOTHING;

INSERT INTO product (product_id, name, description, price, in_stock, availability, category_id)
VALUES (6, 'Birthday Special', 'Large colorful bouquet.', 1500.00, 10, true, 3)
    ON CONFLICT DO NOTHING;


SELECT setval('product_id_seq', (SELECT MAX(product_id) FROM product));