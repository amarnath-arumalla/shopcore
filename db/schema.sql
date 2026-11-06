-- ShopCore database schema
-- Run as the app user:  psql -U shopcore_app -d shopcore -f db/schema.sql

-- Drop children before parents (a line can't exist without its order).
DROP TABLE IF EXISTS order_lines;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS products;

CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku         VARCHAR(20)   NOT NULL UNIQUE,
    name        VARCHAR(200)  NOT NULL CHECK (length(trim(name)) > 0),
    category    VARCHAR(20)   NOT NULL CHECK (category IN ('ELECTRONICS', 'BOOKS', 'CLOTHING', 'HOME')),
    price       NUMERIC(10,2) NOT NULL CHECK (price > 0),
    stock       INTEGER       NOT NULL CHECK (stock >= 0),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE customers (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email       VARCHAR(254)  NOT NULL UNIQUE,
    full_name   VARCHAR(200)  NOT NULL CHECK (length(trim(full_name)) > 0),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_number  VARCHAR(20)   NOT NULL UNIQUE,
    customer_id   BIGINT        NOT NULL REFERENCES customers (id),
    status        VARCHAR(20)   NOT NULL DEFAULT 'PLACED'
                  CHECK (status IN ('PLACED', 'PAID', 'SHIPPED', 'CANCELLED')),
    discount      NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE order_lines (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      BIGINT        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id    BIGINT        NOT NULL REFERENCES products (id),
    product_name  VARCHAR(200)  NOT NULL,
    unit_price    NUMERIC(10,2) NOT NULL CHECK (unit_price > 0),
    quantity      INTEGER       NOT NULL CHECK (quantity > 0),
    UNIQUE (order_id, product_id)
);

-- "Show this customer's orders, newest first"
CREATE INDEX idx_orders_customer_created ON orders (customer_id, created_at DESC);

-- "Which orders contain this product?" (also speeds up the foreign key check when a product is deleted)
CREATE INDEX idx_order_lines_product ON order_lines (product_id);
