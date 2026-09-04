package com.hotelio.booking.client

final case class PromoInfo(code: String, discount: Double)

// Интерфейс для доступа к монолиту
trait MonolithClient[F[_]] {
  def isUserActive(userId: String): F[Boolean]
  def isUserBlacklisted(userId: String): F[Boolean]
  def userStatus(userId: String): F[Option[String]]

  def isHotelOperational(hotelId: String): F[Boolean]
  def isHotelFullyBooked(hotelId: String): F[Boolean]
  def isHotelTrusted(hotelId: String): F[Boolean]

  def validatePromo(code: String, userId: String): F[Option[PromoInfo]]
}
