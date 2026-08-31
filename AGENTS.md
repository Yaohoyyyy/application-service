# AGENTS.md — restProject

## Stack
- Spring Boot 4.1.0, Java 21, Maven (wrapper via `./mvnw`), PostgreSQL, Kafka

## Setup & run
- Requires PostgreSQL at `localhost:5432` (user/pass: `postgres/postgres`) and Kafka at `localhost:9092`
- Build: `./mvnw compile`
- Run: `./mvnw spring-boot:run`
- Test: `./mvnw test`
- Compile/test errors about "release version 21 not supported" = JDK mismatch in the environment, not a code issue

## Testing
- **After any changes, run the AT project tests** at `D:\Work\Projects\AT`:
  ```
  cd D:\Work\Projects\AT
  mvn test
  ```
  The AT project sends HTTP requests to the running app (`localhost:8080`). Make sure the app is running before executing tests.
- **Display the test results in the chat** — after running the AT tests, output the test results (which tests passed/failed) to the user in the chat.
- **DO NOT modify files in the AT project** — the AT tests are the source of truth for validation. Only the user can change them.

## Config
- **Two config files** coexist — `application.properties` (DB, JPA, server port) and `application.yml` (Kafka). Keep both in sync.
- `spring.jpa.hibernate.ddl-auto=update` — JPA manages schema automatically

## Known issues (do not reintroduce)
1. **`spring-boot-starter-webmvc`** in `pom.xml:35` does not exist. The correct artifact is `spring-boot-starter-web`. Same for `spring-boot-starter-webmvc-test` → `spring-boot-starter-test`.
2. **Package typo**: `com.application.contoller` — all controllers live under this misspelled package, do not "fix" without also moving existing files

## Fixed issues
- `@Valid` was missing on `@RequestBody` in `UserController` — added
- `GlobalExceptionHandler` now uses `@RestControllerAdvice` and returns `{"message":"..."}` (400 for business errors, 400 for validation errors, 404 for unknown routes)
- `UserDto.updatedAt` now correctly reads `getUpdatedAt()` (was `getCreatedAt()`)

## Kafka
- **Consumer**: manual Java config in `KafkaConsumerConfig` — consumes `TEST.IN.TOPIC`, deserializes JSON to `UserMessage`
- **Producer**: manual `KafkaProducerConfig` creates `KafkaTemplate<String, String>` bean — `KafkaAutoConfiguration` does NOT reliably auto-configure it in this project
- Topics: `TEST.IN.TOPIC` (consumer), `TEST.OUT.TOPIC` (producer, via `TextController`)
- All broker configs hardcode `localhost:9092`

## Code conventions
- **Lombok is inconsistent**: `UserMessage` uses `@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`; `User` uses manual getters/setters. Follow the pattern of the file you're editing.
- `@Transactional` import: `jakarta.transaction.Transactional` (not Spring's `org.springframework.transaction.annotation.Transactional`)
- DTOs: `UserDto` is a `record` with `fromEntity()` factory and `toEntity()` converter
- DTOs for request/response: Prefer `record` for new DTOs (see `TextRequest`).

## API
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | Login (body: `{"login":"...","password":"..."}`) → returns JWT token |
| GET | `/api/users` | List all users (requires auth) |
| GET | `/api/users/{id}` | Get user by id (requires auth) |
| POST | `/api/users` | Create user (`UserDto` body, requires auth) |
| POST | `/api/text` | Send text to `TEST.OUT.TOPIC` (`{"text":"..."}` body) |

## Auth
- `POST /api/auth/login` returns a JWT token if login+password match an `employees` record
- All `/api/users` endpoints require `Authorization: Bearer <token>` header
- Token is validated against the `tokens` table in DB
- Auth endpoint itself is open (no token required)
- Before testing `/api/users`, first get a token via `POST /api/auth/login`

## Error response format
All errors return `{"message":"<description>"}` with appropriate HTTP status:
- 400 — business errors (e.g. duplicate email) and validation errors
- 404 — resource not found

## Service layer
- `UserService.createUser(UserDto)` — single creation method, invoked from both the REST controller and Kafka listener
- `UserService.getAllUsers()` returns `List<User>` (entities), controller maps to DTOs
