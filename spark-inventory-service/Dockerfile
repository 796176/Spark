FROM eclipse-temurin:25-noble AS builder
WORKDIR /opt/spark

ADD https://archive.apache.org/dist/maven/maven-3/3.9.15/binaries/apache-maven-3.9.15-bin.tar.gz maven.tar.gz
RUN mkdir maven
RUN tar -xf maven.tar.gz -C maven --strip-components=1

COPY pom.xml ./
COPY spark-authorization/pom.xml spark-authorization/
RUN ./maven/bin/mvn -f spark-authorization/pom.xml dependency:go-offline
COPY spark-inventory-service/pom.xml spark-inventory-service/
RUN ./maven/bin/mvn -f spark-inventory-service/pom.xml dependency:go-offline

COPY spark-account-service/pom.xml spark-account-service/
COPY spark-order-service/pom.xml spark-order-service/
COPY spark-web-gateway/pom.xml spark-web-gateway/

COPY spark-authorization/src spark-authorization/src/
COPY spark-inventory-service/src spark-inventory-service/src/
RUN ./maven/bin/mvn --also-make --projects spark-inventory-service -o package
RUN ["/bin/bash", "-c", \
"java -Djarmode=tools -jar $(find spark-inventory-service/target/spark-inventory-service-*.jar) extract --layers \
--destination extracted"]

FROM eclipse-temurin:25-jre-noble AS final
WORKDIR /opt/spark

COPY --from=builder /opt/spark/extracted/dependencies/ ./
COPY --from=builder /opt/spark/extracted/spring-boot-loader/ ./
COPY --from=builder /opt/spark/extracted/snapshot-dependencies/ ./
COPY --from=builder /opt/spark/extracted/application/ ./

ENV JVM_OPTIONS=
ENV DB_USER=postgres DB_PASSWORD=postgres DB_URL=jdbc:postgresql://postgres/spark_inventory_service
ENV RMQ_USER=guest RMQ_PASSWORD=guest RMQ_ADDRESS=rabbitmq RMQ_PORT=5672
ENTRYPOINT ["/bin/bash", "-c", \
"java $JVM_OPTIONS -Dspring.datasource.url=$DB_URL -Dspring.datasource.username=$DB_USER \
-Dspring.datasource.password=$DB_PASSWORD -Dorg.example.spark.rmq.address=$RMQ_ADDRESS \
-Dorg.example.spark.rmq.port=$RMQ_PORT -Dorg.example.spark.rmq.username=$RMQ_USER \
-Dorg.example.spark.rmq.password=$RMQ_PASSWORD -jar $(find spark-inventory-service-*.jar)"]
