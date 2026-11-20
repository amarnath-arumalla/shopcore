-- "My orders" keyset pagination:
--   WHERE customer_id = ? AND (created_at, id) < (?, ?) ORDER BY created_at DESC, id DESC
CREATE INDEX idx_orders_customer_created_id ON orders (customer_id, created_at DESC, id DESC);

-- The old index is a prefix of the new one, so it is now redundant (it only slows down INSERTs)
DROP INDEX idx_orders_customer_created;