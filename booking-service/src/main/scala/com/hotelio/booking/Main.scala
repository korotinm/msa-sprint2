package com.hotelio.booking

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import com.hotelio.booking.client.MonolithClientHttp4s
import com.hotelio.booking.config.{
  DbConfig,
  KafkaConfig,
  MonolithConfig,
  ServerConfig
}
import com.hotelio.booking.db.{BookingRepository, Database, Migrations}
import com.hotelio.booking.kafka.BookingEventProducer
import com.hotelio.booking.service.BookingService
import com.hotelio.booking.service.grpc.BookingGrpcService
import com.hotelio.proto.booking.booking.BookingServiceFs2Grpc
import fs2.grpc.syntax.all._
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.typelevel.log4cats.{Logger, LoggerFactory}
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {

  private implicit val loggerFactory: LoggerFactory[IO] =
    Slf4jFactory.create[IO]
  private implicit val logger: Logger[IO] = loggerFactory.getLogger

  def run(args: List[String]): IO[ExitCode] =
    for {
      dbCfg <- DbConfig.fromEnv[IO]
      kafkaCfg <- KafkaConfig.fromEnv[IO]
      monolithCfg <- MonolithConfig.fromEnv[IO]
      serverCfg <- ServerConfig.fromEnv[IO]
      _ <- Retry[IO, Unit]("flyway")(Migrations.run[IO](dbCfg).void)
      _ <- server(dbCfg, kafkaCfg, monolithCfg, serverCfg).use { _ =>
        logger.info(s"booking-service: gRPC :${serverCfg.port}") *> IO.never
      }
    } yield ExitCode.Success

  private def server(
      dbCfg: DbConfig,
      kafkaCfg: KafkaConfig,
      monolithCfg: MonolithConfig,
      serverCfg: ServerConfig
  ): Resource[IO, Server] =
    for {
      xa <- Database.transactor[IO](dbCfg)
      _ <- Resource.eval(Database.ping[IO](xa))
      producer <- BookingEventProducer.resource[IO](kafkaCfg)
      monolith <- MonolithClientHttp4s.resource[IO](monolithCfg)
      repo = BookingRepository[IO](xa)
      service = BookingService[IO](repo, monolith, producer)
      grpc = new BookingGrpcService[IO](service)
      svcDef <- BookingServiceFs2Grpc.bindServiceResource[IO](grpc)
      srv <- NettyServerBuilder
        .forPort(serverCfg.port)
        .addService(svcDef)
        .resource[IO]
        .evalTap(s => IO.blocking(s.start()))
    } yield srv
}
