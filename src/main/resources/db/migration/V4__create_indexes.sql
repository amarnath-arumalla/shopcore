-- PostgreSQL indexes PRIMARY KEY and UNIQUE columns automatically, but NOT foreign keys.

-- "Show this customer's orders, newest first"
CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at DESC);

-- "Which orders contain this product?" (also speeds up the foreign key check when a product is deleted)
CREATE INDEX idx_order_lines_product ON order_lines (product_id);