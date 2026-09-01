package com.hotelio.history.domain

import java.time.Instant

// представление события BookingCreated
final case class BookingHistoryRecord(
    bookingId: String,
    userId: String,
    hotelId: String,
    promoCode: Option[String],
    discountPercent: Double,
    price: Double,
    bookingCreatedAt: Instant
)
