package com.hotelio.booking.domain

import java.time.Instant

final case class NewBooking(
    userId: String,
    hotelId: String,
    promoCode: Option[String],
    discountPercent: Double,
    price: Double
)

final case class Booking(
    id: Long,
    userId: String,
    hotelId: String,
    promoCode: Option[String],
    discountPercent: Double,
    price: Double,
    createdAt: Instant
)
