FROM maven:3.6.3-jdk-11-slim AS MAVEN_BUILD

COPY pom.xml /build/
COPY sonar-project.properties /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn package -Dmaven.test.skip=true

FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/hotel-controller-0.0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "hotel-controller-0.0.1-SNAPSHOT.jar"]
