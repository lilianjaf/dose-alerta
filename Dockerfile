FROM eclipse-temurin:26-jdk-alpine AS build
WORKDIR /workspace
COPY . .
ARG MODULE
RUN ./gradlew --no-daemon :${MODULE}:bootJar -x test -x checkstyleMain -x checkstyleTest \
    && cp ${MODULE}/build/libs/${MODULE}-*-SNAPSHOT.jar /workspace/app.jar

FROM eclipse-temurin:26-jre-alpine
ENV TZ=America/Sao_Paulo
RUN apk add --no-cache tzdata \
    && addgroup -S dosealerta \
    && adduser -S -G dosealerta dosealerta
WORKDIR /app
COPY --from=build --chown=dosealerta:dosealerta /workspace/app.jar app.jar
USER dosealerta
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
