package com.hotelio.history.db

import cats.effect.Sync
import cats.syntax.all._
import com.hotelio.history.config.DbConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Migrations {

  private val Locations = "classpath:db/migration"

  def run[F[_]: Sync](cfg: DbConfig): F[MigrateResult] =
    for {
      logger <- Slf4jLogger.create[F]
      _ <- logger.info(s"Flyway start: ${cfg.jdbcUrl}")
      result <- Sync[F].blocking(load(cfg).migrate())
      _ <- logger.info(s"Flyway finish: success=${result.success}")
    } yield result

  private def load(cfg: DbConfig): Flyway =
    Flyway
      .configure()
      .dataSource(cfg.jdbcUrl, cfg.user, cfg.password)
      .locations(Locations)
      .baselineOnMigrate(true)
      .load()
}
