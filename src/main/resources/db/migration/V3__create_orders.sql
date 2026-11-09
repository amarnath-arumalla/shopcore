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