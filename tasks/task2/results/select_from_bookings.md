## БД микросервиса бронировани

`> docker compose exec -T booking-db psql -U booking -d booking -c "select * from bookings;"`

```
 id |   user_id   |   hotel_id   | promo_code | discount_percent | price |          created_at
----+-------------+--------------+------------+------------------+-------+-------------------------------
  1 | test-user-3 | test-hotel-1 |            |                0 |    80 | 2026-09-01 19:17:24.235368+00
  2 | test-user-2 | test-hotel-1 | TESTCODE1  |               10 |    90 | 2026-09-01 19:17:24.335784+00
(2 rows)
```

---

## БД монолита - таблица бронирования

`> docker compose exec -T monolith-db psql -U hotelio -d hotelio -c "select * from booking;"`

```
 discount_percent | price |          created_at           | id |   hotel_id   | promo_code |   user_id
------------------+-------+-----------[--------------------+----+--------------+------------+-------------
               10 |    90 | 2026-09-01 19:17:22.648326+00 | 17 | test-hotel-1 | TESTCODE1  | test-user-2
                0 |    80 | 2026-09-01 19:17:22.648326+00 | 18 | test-hotel-1 |            | test-user-3
(2 rows)
```
