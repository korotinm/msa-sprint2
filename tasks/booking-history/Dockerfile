# Чтобы не тянуть jar сервиса history

FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS builder
WORKDIR /repo

# Сначала только sbt слой — sbt update кешируется отдельным слоем
COPY booking-history-service/project booking-history-service/project
COPY booking-history-service/build.sbt booking-history-service/build.sbt
WORKDIR /repo/booking-history-service
RUN sbt update

WORKDIR /repo
COPY tasks/task2/*.proto tasks/task2/
COPY booking-history-service/src booking-history-service/src
WORKDIR /repo/booking-history-service
RUN sbt assembly

# --- runtime ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /repo/booking-history-service/target/scala-2.13/booking-history-service.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
