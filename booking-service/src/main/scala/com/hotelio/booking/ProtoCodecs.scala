package com.hotelio.booking

import com.hotelio.booking.domain.Booking
import com.hotelio.proto.booking.booking.BookingResponse


object ProtoCodecs {
  def bookingResponse(b: Booking): BookingResponse =
    BookingResponse(
      id = b.id.toString,
      userId = b.userId,
      hotelId = b.hotelId,
      promoCode = b.promoCode.getOrElse(""),
      discountPercent = b.discountPercent,
      price = b.price,
      createdAt = b.createdAt.toString // ISO-8601
    )
}
