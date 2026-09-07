package com.hotelio.booking.db

import cats.effect.MonadCancelThrow
import com.hotelio.booking.domain.{Booking, NewBooking}
import doobie._
import doobie.implicits._
// Meta[Instant] для timestamptz — pg-версия (через OffsetDateTime); НЕ doobie.implicits.javatimedrivernative,
// там getObject(_, Instant.class), который pg-драйвер не поддерживает
import doobie.postgres.implicits._

trait BookingRepository[F[_]] {

  def create(booking: NewBooking): F[Booking]

  def findByUser(userId: String): F[List[Booking]]

  def findAll: F[List[Booking]]
}

object BookingRepository {

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): BookingRepository[F] =
    new Doobie[F](xa)

  private final class Doobie[F[_]: MonadCancelThrow](xa: Transactor[F])
      extends BookingRepository[F] {

    def create(b: NewBooking): F[Booking] =
      Sql.insert(b).transact(xa)

    def findByUser(userId: String): F[List[Booking]] =
      Sql.byUser(userId).to[List].transact(xa)

    def findAll: F[List[Booking]] =
      Sql.all.to[List].transact(xa)
  }

  private object Sql {

    private val selectColumns =
      fr"id, user_id, hotel_id, promo_code, COALESCE(discount_percent, 0.0), price, created_at"

    def insert(b: NewBooking): ConnectionIO[Booking] =
      sql"""
        INSERT INTO bookings (user_id, hotel_id, promo_code, discount_percent, price)
        VALUES (${b.userId}, ${b.hotelId}, ${b.promoCode}, ${b.discountPercent}, ${b.price})
      """.update
        .withUniqueGeneratedKeys[Booking](
          "id",
          "user_id",
          "hotel_id",
          "promo_code",
          "discount_percent",
          "price",
          "created_at"
        )

    def byUser(userId: String): Query0[Booking] =
      (fr"SELECT" ++ selectColumns ++ fr"FROM bookings WHERE user_id = $userId ORDER BY created_at")
        .query[Booking]

    def all: Query0[Booking] =
      (fr"SELECT" ++ selectColumns ++ fr"FROM bookings ORDER BY created_at")
        .query[Booking]
  }
}
