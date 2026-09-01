ThisBuild / scalaVersion := "2.13.18"
ThisBuild / organization := "com.hotelio"
ThisBuild / version      := "1.0.0"

lazy val V = new {
  val catsEffect   = "3.5.7"
  val fs2          = "3.11.0"
  val http4s       = "0.23.30"
  val circe        = "0.14.10"
  val log4cats     = "2.7.0"
  val logback      = "1.5.13"
  val fs2Kafka     = "3.6.0"
  val doobie       = "1.0.0-RC5"
  val flyway       = "10.20.1"
  val munit        = "1.0.3"
  val munitCE      = "2.0.0"
}

lazy val root = (project in file("."))
  .enablePlugins(Fs2Grpc)
  .settings(
    name := "booking-service",

    //proto контракт
    Compile / PB.protoSources += baseDirectory.value / ".." / "tasks" / "task2",

    libraryDependencies ++= Seq(
      "io.grpc"        % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,

      "org.typelevel" %% "cats-effect"       % V.catsEffect,
      "co.fs2"        %% "fs2-core"          % V.fs2,
      "co.fs2"        %% "fs2-io"            % V.fs2,

      // --- для REST-вызовов монолита
      "org.http4s"    %% "http4s-ember-client" % V.http4s,
      "org.http4s"    %% "http4s-circe"        % V.http4s,
      "org.http4s"    %% "http4s-dsl"          % V.http4s,
      "io.circe"      %% "circe-generic"       % V.circe,

      // Kafka: событие BookingCreated
      "com.github.fd4s" %% "fs2-kafka"        % V.fs2Kafka,

      // логирование
      "org.typelevel"  %% "log4cats-slf4j"    % V.log4cats,
      "ch.qos.logback" %  "logback-classic"   % V.logback,

      // PostgreSQL: доступ к данным + пул
      "org.tpolecat"   %% "doobie-core"       % V.doobie,
      "org.tpolecat"   %% "doobie-hikari"     % V.doobie,
      "org.tpolecat"   %% "doobie-postgres"   % V.doobie, // тянет org.postgresql % postgresql

      // PostgreSQL: миграции схемы
      "org.flywaydb"   %  "flyway-core"                % V.flyway,
      "org.flywaydb"   %  "flyway-database-postgresql" % V.flyway,
    ),

    //fat jar
    Compile / mainClass       := Some("com.hotelio.booking.Main"),
    assembly / assemblyJarName := "booking-service.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _ @ _*) => MergeStrategy.concat  // SLF4J provider, Flyway-плагины
      case PathList("META-INF", _*)                 => MergeStrategy.discard
      case "reference.conf"                         => MergeStrategy.concat
      case _                                        => MergeStrategy.first
    }
  )
