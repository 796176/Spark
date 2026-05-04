FROM eclipse-temurin:25-noble AS builder
WORKDIR /opt/spark

ADD https://archive.apache.org/dist/maven/maven-3/3.9.15/binaries/apache-maven-3.9.15-bin.tar.gz maven.tar.gz
RUN mkdir maven
RUN tar -xf maven.tar.gz -C maven --strip-components=1

COPY pom.xml ./
COPY spark-authorization/pom.xml spark-authorization/
COPY spark-web-gateway/pom.xml spark-web-gateway/

COPY spark-account-service/pom.xml spark-account-service/
COPY spark-inventory-service/pom.xml spark-inventory-service/
COPY spark-order-service/pom.xml spark-order-service/

RUN ./maven/bin/mvn --also-make --projects spark-authorization dependency:resolve-plugins dependency:go-offline -B
RUN ./maven/bin/mvn --also-make --projects spark-web-gateway dependency:resolve-plugins dependency:go-offline -B

COPY spark-authorization/src spark-authorization/src/
COPY spark-web-gateway/src spark-web-gateway/src/
RUN ./maven/bin/mvn --also-make --projects spark-web-gateway -o package

FROM eclipse-temurin:25-jre-noble AS final
WORKDIR /opt/spark

COPY --from=builder /opt/spark/spark-web-gateway/target/spark-web-gateway-*.war ./

ENV JVM_OPTIONS=
ENV DB_USER=postgres DB_PASSWORD=postgres DB_URL=jdbc:postgresql://postgres/spark_web_gateway
ENV RMQ_USER=guest RMQ_PASSWORD=guest RMQ_ADDRESS=rabbitmq RMQ_PORT=5672
ENV LISTENING_PORT=8080
ENTRYPOINT ["/bin/bash", "-c", \
"java $JVM_OPTIONS -Dspring.datasource.url=$DB_URL -Dspring.datasource.username=$DB_USER \
-Dspring.datasource.password=$DB_PASSWORD -Dorg.example.spark.rmq.address=$RMQ_ADDRESS \
-Dorg.example.spark.rmq.port=$RMQ_PORT -Dorg.example.spark.rmq.username=$RMQ_USER \
-Dorg.example.spark.rmq.password=$RMQ_PASSWORD -Dserver.port=$LISTENING_PORT -jar $(find spark-web-gateway-*.war)"]
