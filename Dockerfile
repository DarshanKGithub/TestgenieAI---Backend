FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

COPY pom.xml pom.xml
COPY src src
COPY playwright-worker playwright-worker

RUN apt-get update && apt-get install -y curl ca-certificates && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    cd playwright-worker && npm ci && npx playwright install --with-deps chromium && \
    mvn -DskipTests clean package && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

CMD ["java", "-jar", "target/backend-0.0.1-SNAPSHOT.jar"]
