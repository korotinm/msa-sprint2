ThisBuild / scalaVersion := "2.13.18"
ThisBuild / organization := "com.hotelio"
ThisBuild / version      := "1.0.0"

lazy val V = new {
  val catsEffect = "3.5.7"
  val fs2        = "3.11.0"
  val fs2Kafka   = "3.6.0"
  val doobie     = "1.0.0-RC5"
  val flyway     = "10.20.1"
  val log4cats   = "2.7.0"
  val logback    = "1.5.13"
}

lazy val root = (project in file("."))
  .settings(
    name := "booking-history-service",

    // тот же контракт, что у booking-service — но нужны только сообщения (BookingResponse)
    Compile / PB.protoSources += baseDirectory.value / ".." / "tasks" / "task2",
    Compile / PB.targets := Seq(
      // grpc = false: booking.proto содержит service-блок, но нам нужны только сообщения
      scalapb.gen(grpc = false) -> (Compile / sourceManaged).value / "scalapb"
    ),

    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,

      "org.typelevel" %% "cats-effect" % V.catsEffect,
      "co.fs2"        %% "fs2-core"    % V.fs2,
      "co.fs2"        %% "fs2-io"      % V.fs2,

      // Kafka consumer
      "com.github.fd4s" %% "fs2-kafka" % V.fs2Kafka,

      // PostgreSQL: своя БД истории + пул
      "org.tpolecat" %% "doobie-core"     % V.doobie,
      "org.tpolecat" %% "doobie-hikari"   % V.doobie,
      "org.tpolecat" %% "doobie-postgres" % V.doobie, // тянет org.postgresql % postgresql

      // миграции схемы
      "org.flywaydb" % "flyway-core"                % V.flyway,
      "org.flywaydb" % "flyway-database-postgresql" % V.flyway,

      // логи
      "org.typelevel"  %% "log4cats-slf4j"  % V.log4cats,
      "ch.qos.logback" %  "logback-classic" % V.logback
    ),

    //fat-jar
    Compile / mainClass        := Some("com.hotelio.history.Main"),
    assembly / assemblyJarName := "booking-history-service.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _ @ _*) => MergeStrategy.concat // SLF4J provider, Flyway-плагины
      case PathList("META-INF", _*)                 => MergeStrategy.discard
      case "reference.conf"                         => MergeStrategy.concat
      case _                                        => MergeStrategy.first
    }
  )
