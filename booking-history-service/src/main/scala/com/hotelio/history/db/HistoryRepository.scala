package com.hotelio.history.db

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import com.hotelio.history.domain.BookingHistoryRecord
import doobie._
import doobie.implicits._
import doobie.implicits.javatimedrivernative._ // Instant <-> timestamptz

trait HistoryRepository[F[_]] {

  def save(record: BookingHistoryRecord): F[Boolean]

  def count: F[Long]
}

object HistoryRepository {

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): HistoryRepository[F] =
    new Doobie[F](xa)

  private final class Doobie[F[_]: MonadCancelThrow](xa: Transactor[F])
      extends HistoryRepository[F] {

    def save(r: BookingHistoryRecord): F[Boolean] =
      sql"""
        INSERT INTO booking_history
          (booking_id, user_id, hotel_id, promo_code, discount_percent, price, booking_created_at)
        VALUES
          (${r.bookingId}, ${r.userId}, ${r.hotelId}, ${r.promoCode},
           ${r.discountPercent}, ${r.price}, ${r.bookingCreatedAt})
        ON CONFLICT (booking_id) DO NOTHING
      """.update.run.map(_ > 0).transact(xa)

    def count: F[Long] =
      sql"SELECT count(*) FROM booking_history".query[Long].unique.transact(xa)
  }
}
