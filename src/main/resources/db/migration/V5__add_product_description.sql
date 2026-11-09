-- Products can now have an optional description.
-- Existing products get NULL (no description yet). No data is lost.
ALTER TABLE products ADD COLUMN description TEXT;