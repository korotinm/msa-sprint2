package com.hotelio.booking.config

import cats.effect.Sync

final case class KafkaConfig(
    bootstrapServers: String,
    bookingEventsTopic: String
)

object KafkaConfig {

  def fromEnv[F[_]: Sync]: F[KafkaConfig] = Sync[F].delay {
    val env = sys.env
    def str(key: String, default: String): String = env.getOrElse(key, default)

    KafkaConfig(
      bootstrapServers = str("BOOKING_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
      bookingEventsTopic = str("BOOKING_KAFKA_TOPIC", "booking-events")
    )
  }
}
