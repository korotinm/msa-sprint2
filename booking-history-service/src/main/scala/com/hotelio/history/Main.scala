package com.hotelio.history

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import com.hotelio.history.config.{DbConfig, KafkaConfig}
import com.hotelio.history.db.{Database, HistoryRepository, Migrations}
import com.hotelio.history.kafka.HistoryConsumer
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.{Logger, LoggerFactory}

object Main extends IOApp {

  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
  private implicit val logger: Logger[IO]               = loggerFactory.getLogger

  def run(args: List[String]): IO[ExitCode] =
    for {
      dbCfg    <- DbConfig.fromEnv[IO]
      kafkaCfg <- KafkaConfig.fromEnv[IO]
      _        <- Retry[IO, Unit]("flyway")(Migrations.run[IO](dbCfg).void)
      _ <- Database.transactor[IO](dbCfg).use { xa =>
             Database.ping[IO](xa) *>
               logger.info(s"booking-history-service: consuming '${kafkaCfg.topic}'") *>
               // не завершается — держит consumer-стрим, пока контейнер жив
               HistoryConsumer.run[IO](kafkaCfg, HistoryRepository[IO](xa))
           }
    } yield ExitCode.Success
}
