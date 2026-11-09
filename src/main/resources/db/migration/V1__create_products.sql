CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku         VARCHAR(20)   NOT NULL UNIQUE,
    name        VARCHAR(200)  NOT NULL CHECK (length(trim(name)) > 0),
    category    VARCHAR(20)   NOT NULL CHECK (category IN ('ELECTRONICS', 'BOOKS', 'CLOTHING', 'HOME')),
    price       NUMERIC(10,2) NOT NULL CHECK (price > 0),
    stock       INTEGER       NOT NULL CHECK (stock >= 0),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);