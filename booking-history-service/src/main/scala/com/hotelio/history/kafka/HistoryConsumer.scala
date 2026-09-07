package com.hotelio.history.kafka

import scala.concurrent.duration._

import cats.effect.Async
import cats.syntax.all._
import com.hotelio.history.config.KafkaConfig
import com.hotelio.history.db.HistoryRepository
import com.hotelio.history.domain.BookingHistoryRecord
import com.hotelio.proto.booking.booking.BookingResponse
import fs2.kafka._
import org.typelevel.log4cats.Logger

import java.time.Instant

/** Читает топик booking-events, парсит protobuf BookingResponse и складывает в booking_history. */
object HistoryConsumer {

  def run[F[_]: Async: Logger](cfg: KafkaConfig, repo: HistoryRepository[F]): F[Unit] = {
    val settings =
      ConsumerSettings[F, String, Array[Byte]]
        .withBootstrapServers(cfg.bootstrapServers)
        .withGroupId(cfg.groupId)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withEnableAutoCommit(false)

    KafkaConsumer
      .stream(settings)
      .subscribeTo(cfg.topic)
      .records
      .evalMap(committable => handle(repo, committable.record.value).as(committable.offset))
      .through(commitBatchWithin(100, 5.seconds))
      .compile
      .drain
  }

  private def handle[F[_]: Async: Logger](repo: HistoryRepository[F], bytes: Array[Byte]): F[Unit] =
    parse(bytes) match {
      // битое сообщение не должно ронять стрим и застревать в партиции — логируем и идём дальше
      case Left(err) =>
        Logger[F].error(err)("skip malformed BookingCreated event")
      case Right(record) =>
        repo.save(record).flatMap {
          case true  => Logger[F].info(s"history: stored booking ${record.bookingId}")
          case false => Logger[F].debug(s"history: booking ${record.bookingId} already stored")
        }
    }

  private def parse(bytes: Array[Byte]): Either[Throwable, BookingHistoryRecord] =
    Either.catchNonFatal {
      val e = BookingResponse.parseFrom(bytes)
      BookingHistoryRecord(
        bookingId = e.id,
        userId = e.userId,
        hotelId = e.hotelId,
        promoCode = Option(e.promoCode).filter(_.nonEmpty),
        discountPercent = e.discountPercent,
        price = e.price,
        bookingCreatedAt = Instant.parse(e.createdAt)
      )
    }
}
