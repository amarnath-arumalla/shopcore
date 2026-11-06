-- Sample data for local development (fake people, fake emails)
INSERT INTO products (sku, name, category, price, stock) VALUES
    ('SKU-1001', 'Wireless Mouse', 'ELECTRONICS', 19.99, 10),
    ('SKU-1002', 'USB-C Cable',    'ELECTRONICS',  9.99,  3),
    ('SKU-2001', 'Clean Code',     'BOOKS',       35.50,  2),
    ('SKU-2002', 'Effective Java', 'BOOKS',       45.00,  8),
    ('SKU-3001', 'Coffee Mug',     'HOME',        12.00, 25);

INSERT INTO customers (email, full_name) VALUES
    ('alice@example.com', 'Alice Test'),
    ('bob@example.com',   'Bob Test'),
    ('carol@example.com', 'Carol Test');

-- Alice: two orders. Bob: one order. Carol: none yet.
INSERT INTO orders (order_number, customer_id, status, discount) VALUES
    ('ORD-1001', 1, 'SHIPPED',  0),
    ('ORD-1002', 2, 'PAID',     6.50),
    ('ORD-1003', 1, 'PLACED',   0);

INSERT INTO order_lines (order_id, product_id, product_name, unit_price, quantity) VALUES
    (1, 1, 'Wireless Mouse', 19.99, 3),
    (1, 3, 'Clean Code',     35.50, 1),
    (2, 1, 'Wireless Mouse', 19.99, 1),
    (2, 4, 'Effective Java', 45.00, 1),
    (3, 5, 'Coffee Mug',     12.00, 2);