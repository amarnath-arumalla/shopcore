-- Version number for optimistic locking.
-- Every successful update of a product must increase it by 1.
ALTER TABLE products ADD COLUMN version INTEGER NOT NULL DEFAULT 0;