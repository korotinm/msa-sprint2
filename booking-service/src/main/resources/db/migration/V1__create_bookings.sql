CREATE TABLE IF NOT EXISTS bookings (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL,
    hotel_id         VARCHAR(255) NOT NULL,
    promo_code       VARCHAR(255),
    discount_percent DOUBLE PRECISION,
    price            DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS bookings_user_id_idx ON bookings (user_id);
