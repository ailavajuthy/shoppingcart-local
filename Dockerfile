FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package

FROM tomcat:10.1-jdk17-temurin
ARG APP_VERSION=dev
ARG APP_FAULT=false
ENV APP_VERSION=$APP_VERSION APP_FAULT=$APP_FAULT JAVA_OPTS="-Xms64m -Xmx192m"
RUN groupadd -g 10001 app && useradd -u 10001 -g app -M app && \
    rm -rf /usr/local/tomcat/webapps/* && \
    chown -R app:app /usr/local/tomcat
COPY --from=build --chown=app:app /build/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
USER 10001:10001
EXPOSE 8080
CMD ["catalina.sh", "run"]
