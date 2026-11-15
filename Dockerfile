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
# Faster start-up after scaling to zero (QA06): serial GC and C1 only; see application-production.yml.
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:MaxRAMPercentage=75 -Xss512k -Duser.timezone=UTC" \
    SPRING_PROFILES_ACTIVE=production
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/chambita-platform.jar"]
