// ScalaPB: генерация Scala-классов из booking.proto (только сообщения, без gRPC-сервиса)
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.17"

// fat-jar для контейнера
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")
