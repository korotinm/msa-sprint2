package com.hotelio.history.config

import cats.effect.Sync

final case class KafkaConfig(
    bootstrapServers: String,
    topic: String,
    groupId: String
)

object KafkaConfig {

  def fromEnv[F[_]: Sync]: F[KafkaConfig] = Sync[F].delay {
    val env = sys.env
    def str(key: String, default: String): String = env.getOrElse(key, default)

    KafkaConfig(
      bootstrapServers =
        str("HISTORY_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
      // должен совпадать с BOOKING_KAFKA_TOPIC продюсера в booking-service
      topic = str("HISTORY_KAFKA_TOPIC", "booking-events"),
      groupId = str("HISTORY_KAFKA_GROUP_ID", "booking-history-service")
    )
  }
}
