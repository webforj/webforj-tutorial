# Keep this image tag exactly aligned with the Playwright Java dependency in pom.xml.
FROM mcr.microsoft.com/playwright/java:v1.50.0-noble

WORKDIR /app
COPY . /app

ENV CI=true
ENV E2E_IN_DOCKER=true
ENV MAVEN_CONFIG=/var/maven/.m2
RUN mkdir -p /var/maven/.m2

CMD ["mvn", "-ntp", "-Ddocker.e2e.container=true", "verify"]
