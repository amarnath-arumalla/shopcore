CREATE TABLE customers (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email       VARCHAR(254)  NOT NULL UNIQUE,
    full_name   VARCHAR(200)  NOT NULL CHECK (length(trim(full_name)) > 0),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);