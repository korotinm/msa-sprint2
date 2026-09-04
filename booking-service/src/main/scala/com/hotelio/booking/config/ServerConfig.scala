package com.hotelio.booking.config

import cats.effect.Sync

final case class ServerConfig(port: Int)

object ServerConfig {

  def fromEnv[F[_]: Sync]: F[ServerConfig] = Sync[F].delay {
    ServerConfig(sys.env.get("BOOKING_GRPC_PORT").map(_.trim.toInt).getOrElse(9090))
  }
}
