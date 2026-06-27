# AI Content Generator

A production-grade modular monolith backend built with Java 21 and Spring Boot 3.3 for AI-powered content generation, scheduling, authentication, and content management.

---

# Features

- JWT Authentication & Authorization
- Refresh Token System
- AI Content Generation
- Content Persistence
- Scheduled AI Jobs
- Modular Monolith Architecture
- PostgreSQL + Flyway Migrations
- Global Exception Handling
- Bucket4j Rate Limiting
- Clean DTO-based API Design
- Secure Spring Security Configuration
- RESTful API Structure

---

# Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Core Language |
| Spring Boot 3.3 | Backend Framework |
| Spring Security | Authentication & Authorization |
| JWT | Access Authentication |
| PostgreSQL | Database |
| Flyway | Database Migrations |
| JPA / Hibernate | ORM |
| Bucket4j | Rate Limiting |
| Gradle | Build Tool |

---

# Project Architecture

```text
src/main/java/com/aicontentgenerator/

├── AiContentGeneratorApplication.java
│
├── common/
│   ├── response/
│   └── exception/
│
├── user/
│   ├── entity/
│   └── repository/
│
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── ai/
│   ├── config/
│   ├── client/
│   ├── dto/
│   ├── service/
│   └── controller/
│
├── content/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── scheduler/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── infrastructure/
│   ├── security/
│   ├── ratelimit/
│   ├── persistence/
│   ├── exception/
│   └── config/
```

---

# Security

- Stateless JWT Authentication
- Access + Refresh Token Strategy
- Custom JWT Filter
- Spring Security Integration
- Route-based Authorization
- Bucket4j Request Rate Limiting

---

# Rate Limiting

The application uses Bucket4j-based rate limiting with separate strategies:

| Strategy | Purpose |
|---|---|
| AUTH | Authentication endpoints |
| AI | AI generation endpoints |
| GENERAL | General API protection |

Rate limiting is implemented as the first request filter before authentication and controllers.

---

# API Base URL

```http
http://localhost:8080/api/v1
```

---

# Authentication Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Login user |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Revoke refresh token |

---

# AI Endpoints

JWT Required

| Method | Endpoint | Description |
|---|---|---|
| POST | `/ai/generate` | Generate AI content |

---

# Content Endpoints

JWT Required

| Method | Endpoint | Description |
|---|---|---|
| POST | `/content` | Save generated content |
| GET | `/content` | Get all user content |
| GET | `/content/{id}` | Get content by ID |
| DELETE | `/content/{id}` | Delete content |

---

# Scheduler Endpoints

JWT Required

| Method | Endpoint | Description |
|---|---|---|
| POST | `/scheduler` | Schedule AI generation |
| GET | `/scheduler` | Get all scheduled jobs |
| GET | `/scheduler/{id}` | Get scheduled job |
| DELETE | `/scheduler/{id}` | Cancel pending job |

---

# Authentication Header

All secured endpoints require:

```http
Authorization: Bearer <accessToken>
```

---

# Request Examples

## Register

```http
POST /auth/register
```

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

## Login

```http
POST /auth/login
```

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

## Refresh Token

```http
POST /auth/refresh
```

```json
{
  "refreshToken": "uuid-token-here"
}
```

---

## Generate AI Content

```http
POST /ai/generate
```

```json
{
  "prompt": "Write a blog post about Spring Boot"
}
```

---

## Save Content

```http
POST /content
```

```json
{
  "prompt": "Write a blog post about Spring Boot",
  "result": "Here is your blog post..."
}
```

---

## Schedule AI Job

```http
POST /scheduler
```

```json
{
  "prompt": "Write a weekly newsletter",
  "runAt": "2025-12-01T09:00:00"
}
```

---

# API Response Structure

## Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2025-01-01T12:00:00"
}
```

---

## Error Response

```json
{
  "success": false,
  "message": "Something went wrong",
  "timestamp": "2025-01-01T12:00:00"
}
```

---

## Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "must be a valid email address",
    "password": "must be between 8 and 100 characters"
  },
  "timestamp": "2025-01-01T12:00:00"
}
```

---

# HTTP Status Codes

| Code | Meaning |
|---|---|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

---

# Running The Project

## Clone Repository

```bash
git clone <your-repository-url>
```

---

## Configure Database

Update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/aicontentgenerator
    username: postgres
    password: password
```

---

## Run PostgreSQL

Make sure PostgreSQL is running locally.

---

## Start Application

```bash
./gradlew bootRun
```

---

# Future Improvements

- Multi-provider AI support
- Docker deployment
- Redis caching
- Email notifications
- OAuth2 Authentication
- API documentation with Swagger/OpenAPI
- Role-based authorization
- Metrics & monitoring
- CI/CD pipeline

---

# Author

Built with Spring Boot, clean architecture principles, and production-grade backend practices.
