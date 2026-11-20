-- Global "latest orders" keyset pagination (story 27):
--   ORDER BY created_at DESC, id DESC
CREATE INDEX idx_orders_created_id ON orders (created_at DESC, id DESC);