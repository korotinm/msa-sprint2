package com.hotelio.booking.kafka

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.hotelio.booking.ProtoCodecs
import com.hotelio.booking.config.KafkaConfig
import com.hotelio.booking.domain.Booking
import com.hotelio.proto.booking.booking.BookingResponse
import fs2.kafka._
import org.typelevel.log4cats.slf4j.Slf4jLogger

/** Продьюсер для публикации событий - созданной брони.
  *
  * Переиспользуем модел BookingResponse для записи в Kafka
  */
trait BookingEventProducer[F[_]] {
  def publish(booking: Booking): F[Unit]
}

object BookingEventProducer {

  def toEvent(b: Booking): BookingResponse = ProtoCodecs.bookingResponse(b)

  def resource[F[_]: Async](
      cfg: KafkaConfig
  ): Resource[F, BookingEventProducer[F]] = {
    val settings =
      ProducerSettings[F, String, Array[Byte]]
        .withBootstrapServers(cfg.bootstrapServers)
        .withAcks(Acks.All)

    for {
      logger <- Resource.eval(Slf4jLogger.create[F])
      producer <- KafkaProducer.resource(settings)
    } yield new BookingEventProducer[F] {
      def publish(booking: Booking): F[Unit] = {
        val event = toEvent(booking)
        val record = ProducerRecord(
          cfg.bookingEventsTopic,
          booking.id.toString,
          event.toByteArray
        )
        producer.produce(ProducerRecords.one(record)).flatten.void *>
          logger.info(
            s"Kafka: BookingCreated id=${booking.id} -> ${cfg.bookingEventsTopic}"
          )
      }
    }
  }
}
