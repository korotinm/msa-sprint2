package com.hotelio.history.config

import cats.effect.Sync

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
      host = str("HISTORY_DB_HOST", "localhost"),
      port = int("HISTORY_DB_PORT", 5432),
      name = str("HISTORY_DB_NAME", "booking_history"),
      user = str("HISTORY_DB_USER", "history"),
      password = str("HISTORY_DB_PASSWORD", "history"),
      poolSize = int("HISTORY_DB_POOL_SIZE", 5)
    )
  }
}
