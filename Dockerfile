# First stage: complete build environment
FROM maven:3.5.0-jdk-8-alpine AS builder

# add pom.xml and source code
ADD ./pom.xml pom.xml
ADD ./src src/

# package jar
RUN mvn clean package

# Second stage: minimal runtime environment
From openjdk:8-jre-alpine

# copy jar from the first stage
COPY --from=builder target/lib lib/
COPY --from=builder target/testapp-1.0-SNAPSHOT.jar testapp-1.0-SNAPSHOT.jar




CMD ["java", "-jar", "testapp-1.0-SNAPSHOT.jar"]