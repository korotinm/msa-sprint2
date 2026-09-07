`> docker compose exec -T booking-history-db psql -U booking_history -d booking_history -c "select * from booking_history;"`

```
 booking_id |   user_id   |   hotel_id   | promo_code | discount_percent | price |      booking_created_at       |          received_at
------------+-------------+--------------+------------+------------------+-------+-------------------------------+-------------------------------
 1          | test-user-3 | test-hotel-1 |            |                0 |    80 | 2026-09-01 19:17:24.235368+00 | 2026-09-01 19:17:24.26268+00
 2          | test-user-2 | test-hotel-1 | TESTCODE1  |               10 |    90 | 2026-09-01 19:17:24.335784+00 | 2026-09-01 19:17:24.345262+00
(2 rows)
```
