package com.hotelio.booking.config

import cats.effect.Sync
import cats.syntax.all._
import org.http4s.Uri

final case class MonolithConfig(baseUri: Uri)

object MonolithConfig {

  private val Default = "http://localhost:8084"

  def fromEnv[F[_]: Sync]: F[MonolithConfig] =
    Sync[F]
      .delay(sys.env.getOrElse("BOOKING_MONOLITH_BASE_URL", Default))
      .flatMap { raw =>
        Uri.fromString(raw).liftTo[F].map(MonolithConfig(_))
      }
}
