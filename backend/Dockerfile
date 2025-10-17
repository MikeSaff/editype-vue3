# Backend Dockerfile - Multi-stage build with LaTeX support
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage with LaTeX and Pandoc
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install system dependencies
RUN apk update && apk add --no-cache \
    # LaTeX distribution
    texlive \
    texlive-xetex \
    texlive-luatex \
    # Build tools
    make \
    perl \
    # Pandoc for document conversion
    pandoc \
    # Fonts
    fontconfig \
    ttf-dejavu \
    ttf-liberation \
    # PDF tools
    ghostscript \
    # Node.js for Vivliostyle
    nodejs \
    npm \
    # Cleanup
    && rm -rf /var/cache/apk/*

# Install Vivliostyle CLI
RUN npm install -g @vivliostyle/cli

# Create directories for LaTeX templates and temp files
RUN mkdir -p /app/templates /app/temp /root/.texlive

# Set environment variables
ENV TEXLIVE_INSTALLED=true
ENV LATEX_ENGINE=xelatex
ENV PANDOC_VERSION=3.1
ENV VIVLIOSTYLE_INSTALLED=true

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Copy LaTeX templates (will be created later)
COPY src/main/resources/templates/ /app/templates/

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]




