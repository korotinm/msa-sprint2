package com.hotelio.booking.client

import cats.effect.{Async, Concurrent, Resource}
import cats.syntax.all._
import com.hotelio.booking.config.MonolithConfig
import fs2.io.net.Network
import io.circe.Decoder
import org.http4s.Method.{GET, POST}
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.Accept
import org.http4s.{EntityDecoder, MediaRange, Method, Request, Response, Status, Uri}
import org.typelevel.log4cats.Logger

// REST-клиент к монолиту: user / hotel / review / promo
final class MonolithClientHttp4s[F[_]: Concurrent: Logger](
    client: Client[F],
    cfg: MonolithConfig
) extends MonolithClient[F] {

  private val base = cfg.baseUri

  // Эндпоинты монолита отдают boolean/status как application/json (Jackson).
  // client.expect[String] выставил бы Accept: text/*, на что Spring отвечает 406 —
  // поэтому на всех запросах шлём Accept: */* и разбираем тело сами.
  private def request(method: Method, uri: Uri): Request[F] =
    Request[F](method, uri).putHeaders(Accept(MediaRange.`*/*`))

  private implicit val promoInfoDecoder: Decoder[PromoInfo] =
    Decoder.forProduct2("code", "discount")(PromoInfo.apply)
  private implicit val promoInfoEntityDecoder: EntityDecoder[F, PromoInfo] = jsonOf

  def isUserActive(userId: String): F[Boolean] =
    bool(base / "api" / "users" / userId / "active")

  def isUserBlacklisted(userId: String): F[Boolean] =
    bool(base / "api" / "users" / userId / "blacklisted")

  def userStatus(userId: String): F[Option[String]] = {
    val uri = base / "api" / "users" / userId / "status"
    client.run(request(GET, uri)).use {
      case r if r.status.isSuccess         => r.as[String].map(s => Option(s.trim).filter(_.nonEmpty))
      case r if r.status == Status.NotFound => Option.empty[String].pure[F]
      case r                               => unexpected("GET user status", uri, r)
    }
  }

  def isHotelOperational(hotelId: String): F[Boolean] =
    bool(base / "api" / "hotels" / hotelId / "operational")

  def isHotelFullyBooked(hotelId: String): F[Boolean] =
    bool(base / "api" / "hotels" / hotelId / "fully-booked")

  def isHotelTrusted(hotelId: String): F[Boolean] =
    bool(base / "api" / "reviews" / "hotel" / hotelId / "trusted")

  def validatePromo(code: String, userId: String): F[Option[PromoInfo]] = {
    val uri = (base / "api" / "promos" / "validate")
      .withQueryParam("code", code)
      .withQueryParam("userId", userId)
    client.run(request(POST, uri)).use {
      case r if r.status.isSuccess           => r.as[PromoInfo].map(_.some)
      case r if r.status == Status.BadRequest => Option.empty[PromoInfo].pure[F]
      case r                                 => unexpected("POST validate promo", uri, r)
    }
  }

  private def bool(uri: Uri): F[Boolean] =
    client.run(request(GET, uri)).use {
      case r if r.status.isSuccess =>
        r.bodyText.compile.string.flatMap { body =>
          body.trim.toLowerCase match {
            case "true"  => true.pure[F]
            case "false" => false.pure[F]
            case other =>
              Logger[F].error(s"$uri: expected boolean, but received '$other'") *>
                new RuntimeException(s"non-boolean response from $uri").raiseError[F, Boolean]
          }
        }
      case r => unexpected(s"GET ${uri.path}", uri, r)
    }

  private def unexpected[A](op: String, uri: Uri, r: Response[F]): F[A] =
    r.bodyText.compile.string.attempt.flatMap { body =>
      val msg = s"$op $uri: unexpected status ${r.status.code}, body=${body.getOrElse("<none>")}"
      Logger[F].error(msg) *> new RuntimeException(msg).raiseError[F, A]
    }
}

object MonolithClientHttp4s {

  def resource[F[_]: Async: Network: Logger](
      cfg: MonolithConfig
  ): Resource[F, MonolithClient[F]] =
    EmberClientBuilder.default[F].build.map(apply(_, cfg))

  def apply[F[_]: Concurrent: Logger](
      client: Client[F],
      cfg: MonolithConfig
  ): MonolithClient[F] =
    new MonolithClientHttp4s[F](client, cfg)
}
