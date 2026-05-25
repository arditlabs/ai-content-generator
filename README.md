Production-grade modular monolith backend for AI-powered content generation and automation.

---

## Tech Stack

- Java 21
- Spring Boot 3.3
- PostgreSQL
- Spring Security
- JWT Authentication
- Flyway
- Bucket4j Rate Limiting
- Gradle

---

## Architecture

Modules:

- Auth Module
- User Module
- AI Module
- Content Module
- Scheduler Module
- Infrastructure Module

---

## Features

- JWT Authentication + Refresh Tokens
- AI Content Generation
- OpenAI-ready provider abstraction
- Scheduled automation system
- Request rate limiting
- PostgreSQL persistence
- Global exception handling

---

## Project Structure

```text
src/main/java/com/aicontentgenerator
```

(Add your architecture here)

---

## How To Run

### Requirements

- Java 21
- PostgreSQL
- Gradle

### Setup Database

Create PostgreSQL database:

```sql
CREATE DATABASE aicontentgenerator;
```

### Configure application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aicontentgenerator
    username: postgres
    password: your_password
```

### Run Application

```bash
./gradlew bootRun
```

---

## Authentication Flow

User → JWT Login → Protected APIs

---

## AI Flow

Controller → AI Service → Provider Client → Response

---

## Scheduler Flow

Scheduler → Job Executor → Content Generation

---

## Future Improvements

- Redis caching
- Kafka async jobs
- Multi-tenant SaaS support
- Docker deployment

---

## 👤 Author

G G
