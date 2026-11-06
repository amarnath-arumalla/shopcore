-- Loads a LOT of fake data so we can see performance problems.
-- Run AFTER schema.sql and seed.sql:  psql -U shopcore_app -d shopcore -f db/bulk_data.sql

-- 100,000 fake customers
INSERT INTO customers (email, full_name)
SELECT 'customer' || n || '@example.com', 'Customer ' || n
FROM generate_series(1, 100000) AS n;

-- 1,000,000 fake orders, spread randomly across customers and over the last year
INSERT INTO orders (order_number, customer_id, status, created_at)
SELECT 'ORD-' || (100000 + n),
       1 + floor(random() * 100000)::bigint,
       (ARRAY['PLACED', 'PAID', 'SHIPPED', 'CANCELLED'])[1 + floor(random() * 4)::int],
       now() - random() * interval '365 days'
FROM generate_series(1, 1000000) AS n;

-- Refresh the statistics the query planner uses
ANALYZE customers;
ANALYZE orders;