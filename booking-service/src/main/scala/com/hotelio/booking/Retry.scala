package com.hotelio.booking

import scala.concurrent.duration._

import cats.effect.Temporal
import cats.syntax.all._
import org.typelevel.log4cats.Logger

object Retry {

  def apply[F[_]: Temporal: Logger, A](
      label: String,
      attempts: Int = 15,
      delay: FiniteDuration = 3.seconds
  )(fa: F[A]): F[A] =
    fa.handleErrorWith { e =>
      if (attempts <= 1) e.raiseError[F, A]
      else
        Logger[F].warn(
          s"$label: ${e.getMessage}; retry in $delay ($attempts left)"
        ) *>
          Temporal[F].sleep(delay) *> apply(label, attempts - 1, delay)(fa)
    }
}
