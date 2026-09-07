# Чтобы не тянуть jar сервиса booking

FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS builder
WORKDIR /repo

# Сначала только sbt слой — sbt update кешируется отдельным слоем
COPY booking-service/project booking-service/project
COPY booking-service/build.sbt booking-service/build.sbt
WORKDIR /repo/booking-service
RUN sbt update

WORKDIR /repo
COPY tasks/task2/*.proto tasks/task2/
COPY booking-service/src booking-service/src
WORKDIR /repo/booking-service
RUN sbt assembly

# --- runtime ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /repo/booking-service/target/scala-2.13/booking-service.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
