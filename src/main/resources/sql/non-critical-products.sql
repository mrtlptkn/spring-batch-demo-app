CREATE TABLE IF NOT EXISTS non_criticals_products (
    productId   BIGINT PRIMARY KEY,
    productName   VARCHAR(100) NOT NULL,
    unitPrice     DECIMAL(12,2) NOT NULL,
    unitsInStock int NOT NULL
);
