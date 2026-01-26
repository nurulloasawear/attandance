FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./

COPY common-lib/pom.xml common-lib/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY attendance-service/pom.xml attendance-service/pom.xml

RUN mvn -B -q -DskipTests -pl user-service -am dependency:go-offline

COPY common-lib/src common-lib/src
COPY user-service/src user-service/src

RUN mvn -B -DskipTests -pl user-service -am clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/user-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
