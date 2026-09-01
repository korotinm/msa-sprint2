package com.hotelio.booking.db

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.hotelio.booking.config.DbConfig
import doobie._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Database {

  def transactor[F[_]: Async](cfg: DbConfig): Resource[F, HikariTransactor[F]] =
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[F](cfg.poolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = "org.postgresql.Driver",
        url = cfg.jdbcUrl,
        user = cfg.user,
        pass = cfg.password,
        connectEC = connectEC
      )
    } yield xa

  /** DB healthcheck */
  def ping[F[_]: Async](xa: Transactor[F]): F[Unit] =
    for {
      logger <- Slf4jLogger.create[F]
      _ <- sql"SELECT 1".query[Int].unique.transact(xa)
      _ <- logger.info("DB is alive")
    } yield ()
}
