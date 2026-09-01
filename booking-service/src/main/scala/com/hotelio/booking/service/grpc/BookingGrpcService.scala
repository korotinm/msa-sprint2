package com.hotelio.booking.service.grpc

import cats.MonadThrow
import cats.syntax.all._
import com.hotelio.booking.ProtoCodecs
import com.hotelio.booking.service.{BookingError, BookingService}
import com.hotelio.proto.booking.booking._
import io.grpc.{Metadata, Status}
import org.typelevel.log4cats.Logger

final class BookingGrpcService[F[_]: MonadThrow: Logger](service: BookingService[F])
    extends BookingServiceFs2Grpc[F, Metadata] {

  def createBooking(request: BookingRequest, ctx: Metadata): F[BookingResponse] =
    service
      .create(request.userId, request.hotelId, blankToNone(request.promoCode))
      .map(ProtoCodecs.bookingResponse)
      .onError { case e => Logger[F].warn(e)(s"createBooking failed: user=${request.userId} hotel=${request.hotelId}") }
      .adaptError { case e: BookingError => Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException() }

  def listBookings(request: BookingListRequest, ctx: Metadata): F[BookingListResponse] =
    service.listByUser(request.userId).map(bs => BookingListResponse(bs.map(ProtoCodecs.bookingResponse)))

  private def blankToNone(s: String): Option[String] =
    Option(s).map(_.trim).filter(_.nonEmpty)
}
