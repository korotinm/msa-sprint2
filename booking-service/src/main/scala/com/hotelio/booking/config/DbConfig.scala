package com.hotelio.booking.config

import cats.effect.Sync

// Конфиг для БД, читаем значения из переменных окружения
final case class DbConfig(
    host: String,
    port: Int,
    name: String,
    user: String,
    password: String,
    poolSize: Int
) {
  val jdbcUrl: String = s"jdbc:postgresql://$host:$port/$name"
}

object DbConfig {

  def fromEnv[F[_]: Sync]: F[DbConfig] = Sync[F].delay {
    val env = sys.env
    def str(key: String, default: String): String = env.getOrElse(key, default)
    def int(key: String, default: Int): Int =
      env.get(key).map(_.trim.toInt).getOrElse(default)

    DbConfig(
      host = str("BOOKING_DB_HOST", "localhost"),
      port = int("BOOKING_DB_PORT", 5432),
      name = str("BOOKING_DB_NAME", "booking"),
      user = str("BOOKING_DB_USER", "booking"),
      password = str("BOOKING_DB_PASSWORD", "booking"),
      poolSize = int("BOOKING_DB_POOL_SIZE", 10)
    )
  }
}
