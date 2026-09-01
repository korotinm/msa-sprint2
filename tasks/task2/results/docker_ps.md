`mikhail.korotin task2 % docker ps`

```
CONTAINER ID   IMAGE                             COMMAND                  CREATED         STATUS         PORTS                                         NAMES
27d346ea3ea6   task2-booking-service             "java -jar app.jar"      4 minutes ago   Up 4 minutes   0.0.0.0:9090->9090/tcp, [::]:9090->9090/tcp   booking-service
e4dea1f1543d   task2-booking-history-service     "java -jar app.jar"      4 minutes ago   Up 4 minutes                                                 booking-history-service
0e7b9e185cdc   confluentinc/cp-kafka:7.2.1       "/etc/confluent/dock…"   4 minutes ago   Up 4 minutes   0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp   task2-kafka-1
d3411f724338   task2-monolith                    "java -jar app.jar"      4 minutes ago   Up 4 minutes   0.0.0.0:8084->8080/tcp, [::]:8084->8080/tcp   hotelio-monolith
c776b785ce05   postgres:15                       "docker-entrypoint.s…"   4 minutes ago   Up 4 minutes   0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   hotelio-db
a1d887845e5e   postgres:15                       "docker-entrypoint.s…"   4 minutes ago   Up 4 minutes   0.0.0.0:5433->5432/tcp, [::]:5433->5432/tcp   booking-db
792739eaece8   postgres:15                       "docker-entrypoint.s…"   4 minutes ago   Up 4 minutes   0.0.0.0:5434->5432/tcp, [::]:5434->5432/tcp   booking-history-db
8982802ffc76   confluentinc/cp-zookeeper:7.2.1   "/etc/confluent/dock…"   4 minutes ago   Up 4 minutes   0.0.0.0:2181->2181/tcp, [::]:2181->2181/tcp   task2-zookeeper-1
```
