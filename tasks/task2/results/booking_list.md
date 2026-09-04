**REST**

request:
`curl --location 'http://localhost:8084/api/bookings?userId=test-user-3'

response:
```
[
    {
        "id": 1,
        "userId": "test-user-3",
        "hotelId": "test-hotel-1",
        "promoCode": "",
        "discountPercent": 0.0,
        "price": 80.0,
        "createdAt": "2026-09-01T19:17:24.235368Z"
    }
]
```

---

**gRPC**

request:
```
grpcurl \
	-plaintext \
	-emit-defaults \
	-proto '/Users/<your user name>/projects/msa-sprint2/tasks/task2/booking.proto' \
	-import-path '/Users/megafon/projects/msa-sprint2/tasks/task2' \
	-d '{"user_id":"test-user-3"}' \
	'localhost:9090' \
	booking.BookingService.ListBookings
```

reponse:
```
{
    "bookings": [
        {
            "id": "1",
            "user_id": "test-user-3",
            "hotel_id": "test-hotel-1",
            "promo_code": "",
            "discount_percent": 0,
            "price": 80,
            "created_at": "2026-09-01T19:17:24.235368Z"
        }
    ]
}
```
