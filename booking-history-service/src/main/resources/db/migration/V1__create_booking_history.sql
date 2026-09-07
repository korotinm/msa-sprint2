CREATE TABLE IF NOT EXISTS booking_history (
    -- id брони из события как натуральный ключ: повторная доставка того же
    -- сообщения из Kafka не создаёт дубль (INSERT ... ON CONFLICT DO NOTHING)
    booking_id         VARCHAR(255) PRIMARY KEY,
    user_id            VARCHAR(255)     NOT NULL,
    hotel_id           VARCHAR(255)     NOT NULL,
    promo_code         VARCHAR(255),
    discount_percent   DOUBLE PRECISION NOT NULL DEFAULT 0,
    price              DOUBLE PRECISION NOT NULL,
    booking_created_at TIMESTAMPTZ      NOT NULL,          -- created_at из события (ISO-8601)
    received_at        TIMESTAMPTZ      NOT NULL DEFAULT now()  -- когда consumer записал строку
);

CREATE INDEX IF NOT EXISTS idx_booking_history_user  ON booking_history (user_id);
CREATE INDEX IF NOT EXISTS idx_booking_history_hotel ON booking_history (hotel_id);
CREATE INDEX IF NOT EXISTS idx_booking_history_day   ON booking_history (booking_created_at);
