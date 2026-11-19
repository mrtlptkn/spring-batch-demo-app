CREATE TABLE IF NOT EXISTS orders (
    order_id   BIGINT PRIMARY KEY,
    customer   VARCHAR(100) NOT NULL,
    amount     DECIMAL(12,2) NOT NULL,
    order_date DATE NOT NULL
);

-- Hangi tarihte çalıştırıldığı bilgisini tutmak için runDate kolonu ekleniyor; her açılışta güvenle tekrar çalışır.
-- Arşivlerken veriyi ilgili runDate göre arşivlememiz için bu kolonu ekliyoruz.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS runDate DATE;