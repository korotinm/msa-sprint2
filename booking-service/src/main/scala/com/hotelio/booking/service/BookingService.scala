package com.hotelio.booking.service

import cats.MonadThrow
import cats.syntax.all._
import com.hotelio.booking.client.MonolithClient
import com.hotelio.booking.db.BookingRepository
import com.hotelio.booking.domain.{Booking, NewBooking}
import com.hotelio.booking.kafka.BookingEventProducer
import org.typelevel.log4cats.Logger

sealed abstract class BookingError(val message: String)
    extends RuntimeException(message)

object BookingError {
  case object UserInactive extends BookingError("User is inactive")
  case object UserBlacklisted extends BookingError("User is blacklisted")
  case object HotelNotOperational
      extends BookingError("Hotel is not operational")
  case object HotelNotTrusted
      extends BookingError("Hotel is not trusted based on reviews")
  case object HotelFullyBooked extends BookingError("Hotel is fully booked")
}

/** Booking-логика, вынесенная из монолита.
  * Проверки и базовая цена берутся из монолита по REST ([[MonolithClient]]),
  * бронь сохраняется в собственную БД, событие уходит в Kafka.
  */
trait BookingService[F[_]] {
  def create(
      userId: String,
      hotelId: String,
      promoCode: Option[String]
  ): F[Booking]
  def listByUser(userId: String): F[List[Booking]]
}

object BookingService {

  private val VipBasePrice = 80.0
  private val DefaultBasePrice = 100.0

  def apply[F[_]: MonadThrow: Logger](
      repo: BookingRepository[F],
      monolith: MonolithClient[F],
      producer: BookingEventProducer[F]
  ): BookingService[F] = new BookingService[F] {

    def create(
        userId: String,
        hotelId: String,
        promoCode: Option[String]
    ): F[Booking] =
      for {
        _ <- validateUser(userId)
        _ <- validateHotel(hotelId)
        base <- basePrice(userId)
        discount <- promoDiscount(promoCode, userId)
        finalPrice = base - discount
        saved <- repo.create(
          NewBooking(userId, hotelId, promoCode, discount, finalPrice)
        )
        _ <- producer
          .publish(saved)
          .handleErrorWith(e =>
            Logger[F].warn(e)(s"Kafka publish failed for booking ${saved.id}")
          )
        //todo: можно DLQ топик создать при необходимости, для разбора ошибок продьюсирования (пока не требуется)
        _ <- Logger[F].info(
          s"Booking ${saved.id} created: user=$userId hotel=$hotelId base=$base discount=$discount price=$finalPrice"
        )
      } yield saved

    def listByUser(userId: String): F[List[Booking]] = repo.findByUser(userId)

    private def validateUser(userId: String): F[Unit] =
      for {
        active <- monolith.isUserActive(userId)
        _ <- MonadThrow[F].raiseWhen(!active)(BookingError.UserInactive)
        blacklisted <- monolith.isUserBlacklisted(userId)
        _ <- MonadThrow[F].raiseWhen(blacklisted)(BookingError.UserBlacklisted)
      } yield ()

    private def validateHotel(hotelId: String): F[Unit] =
      for {
        operational <- monolith.isHotelOperational(hotelId)
        _ <- MonadThrow[F].raiseWhen(!operational)(
          BookingError.HotelNotOperational
        )
        trusted <- monolith.isHotelTrusted(hotelId)
        _ <- MonadThrow[F].raiseWhen(!trusted)(BookingError.HotelNotTrusted)
        fullyBooked <- monolith.isHotelFullyBooked(hotelId)
        _ <- MonadThrow[F].raiseWhen(fullyBooked)(BookingError.HotelFullyBooked)
      } yield ()

    private def basePrice(userId: String): F[Double] =
      monolith.userStatus(userId).map {
        case Some(status) if status.equalsIgnoreCase("VIP") => VipBasePrice
        case _                                              => DefaultBasePrice
      }

    private def promoDiscount(
        promoCode: Option[String],
        userId: String
    ): F[Double] =
      promoCode
        .flatTraverse(code => monolith.validatePromo(code, userId))
        .map(_.fold(0.0)(_.discount))
  }
}
