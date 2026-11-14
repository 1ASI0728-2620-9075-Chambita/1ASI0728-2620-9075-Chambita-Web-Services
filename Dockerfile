# Chambita RESTful API · container for the Vercel Container runtime (listens on $PORT)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B -DskipTests package \
 && java -Djarmode=tools -jar target/chambita-platform.jar extract --destination /workspace/extracted

FROM eclipse-temurin:21-jre
WORKDIR /app
# Extracted layout (application jar + lib/) starts faster than the nested fat jar.
COPY --from=build /workspace/extracted/ ./
# Faster start-up after scaling to zero (QA06): serial GC, C1 only, lazy beans, no schema validation in production.
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75 -Xss512k -Duser.timezone=UTC" \
    LAZY_INITIALIZATION=true \
    JPA_DDL_AUTO=none \
    JPA_JDBC_METADATA_ACCESS=false \
    DATABASE_POOL_SIZE=3
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/chambita-platform.jar"]
