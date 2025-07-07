# Equation Service

A Spring Boot REST API service for equation processing.

## Requirements

- **Java 21** or higher
- Maven (or use included Maven wrapper)

## 30-Second Quick Start

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Default Endpoints

- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **API Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Tech Stack

- Java 21
- Spring Boot 3.3.13
- Spring Web (REST APIs)
- Spring Validation
- Spring Actuator (Monitoring)
- H2 Database (In-memory)
- SpringDoc OpenAPI (API Documentation)

## Development

### Build
```bash
./mvnw clean compile
```

### Test
```bash
./mvnw test
```

### Package
```bash
./mvnw package
``` 