# AGENTS.md — restProject

## Stack
- Spring Boot 4.1.0, Java 21, Maven (wrapper via `./mvnw`)
- PostgreSQL (JPA/Hibernate), Kafka, Redis (cache), MongoDB (catalog), springdoc-openapi 3.1.1 (Swagger UI)

## Setup & run
- Local dependencies: PostgreSQL `localhost:5432` (postgres/postgres), Kafka `localhost:9092`, Redis `localhost:6379`, MongoDB `localhost:27017/shop_db`
- External jur.лица service: `http://localhost:8089` (`app.external.jur.base-url` in `application.properties`)
- Build: `./mvnw compile`
- Run: `./mvnw spring-boot:run`
- Test: `./mvnw test`
- Compile/test errors about "release version 21 not supported" = JDK mismatch in the environment, not a code issue

## Docker
- `docker-compose.yml` starts postgres, zookeeper, kafka, app (env vars override DB/Kafka settings)
- `Dockerfile` copies `target/*.jar` — run `./mvnw package -DskipTests` **before** `docker compose build`
- `docker compose up --build`

## Testing
- **After any changes, run the tests from this project only** (`restProject`) — no separate AT project:
  ```
  ./mvnw test
  ```
  API tests live in `src/test/java/com/application/api/**` (JUnit 5 + RestAssured). They send HTTP requests to the **running app** (`localhost:8080`). Make sure the app is running before executing tests.
- **Display the test results in the chat** — after running the tests, output which tests passed/failed to the user in the chat.
- Settings for API tests (base URL, login/password of an `employees` record) live in `src/test/resources/api.properties`. Keep credentials in sync with the DB.
- Local unit tests in `src/test` (e.g. `ApplicationTests`, `UserControllerTest`) are `@Disabled` — do not rely on them.
- Surefire is configured in `pom.xml` to also pick up `**/api/**/*.java` test classes (which are not named `*Test`).

## Config
- **Config lives in** `src/main/resources/application.properties` (DB, JPA, MongoDB, jur base-url, port) **and** `application.yml` (Kafka). Keep both in sync.
- Root-level `application.properties` is a docker/env-var template — not on the classpath, not loaded by Spring.
- `spring.jpa.hibernate.ddl-auto=update` — JPA manages schema automatically
- `spring.grpc.server.port=9090` — gRPC Netty-сервер; REST остаётся на `server.port=8080`

## Known issues (do not reintroduce)
1. **`spring-boot-starter-webmvc`** in `pom.xml` does not exist. Correct artifacts: `spring-boot-starter-web`, `spring-boot-starter-test` (note: `spring-boot-webmvc-test` at `pom.xml:100` currently exists and is used — verify before changing).
2. **Package typo**: `com.application.contoller` — all Postgres-side controllers live under this misspelled package, do not "fix" without also moving existing files. Mongo controllers correctly use `com.application.mongo.controller`.
3. **Groovy must be pinned to 4.0.22** (`<groovy.version>4.0.22</groovy.version>` in `pom.xml`). Spring Boot 4.1.0 BOM forces Groovy 5.0.6, which breaks rest-assured 5.5.7 — every GET request dies with `NullPointerException` at `ClosureMetaClass.invokeOnDelegationObject` (`Class.isAssignableFrom`). rest-assured only supports Groovy `[4.0,5.0)`. Do not remove the pin or bump Groovy.
4. **Pre-existing bug, not introduced by gRPC**: `AuthService.login()` writes the JWT to `tokens` on every call, but `tokens.token` is UNIQUE and the JWT payload has only `sub` + `iat` (second precision). Two logins for the same employee within the same second therefore produce byte-identical tokens and the second one fails with a 400 `duplicate key value violates unique constraint`. Affects REST and gRPC alike. Fix by adding a unique claim (e.g. `jti`) to the JWT in `AuthService`, or by making the token save an upsert. Not fixed here because it changes existing REST behaviour.

## Fixed issues
- `@Valid` was missing on `@RequestBody` in `UserController` — added
- `GlobalExceptionHandler` now uses `@RestControllerAdvice` and returns `{"message":"..."}` (400 for business errors, 400 for validation errors, 404 for unknown routes)
- `UserDto.updatedAt` now correctly reads `getUpdatedAt()` (was `getCreatedAt()`)

## Kafka
- **Consumer**: manual Java config in `KafkaConsumerConfig` — consumes `TEST.IN.TOPIC`, deserializes JSON to `UserMessage`
- **Producer**: manual `KafkaProducerConfig` creates `KafkaTemplate<String, String>` bean — `KafkaAutoConfiguration` does NOT reliably auto-configure it in this project
- Topics: `TEST.IN.TOPIC` (consumer), `TEST.OUT.TOPIC` (producer, via `TextController`)
- All broker configs hardcode `localhost:9092` (overridable via `SPRING_KAFKA_*` env vars in docker)

## Redis cache (cards)
- `RedisConfig` (`@EnableCaching`) — `RedisCacheManager`, TTL 10 min, JSON value serializer
- `CardService`: `@Cacheable("cards")` on `getCardDtoById`, `@CachePut` on `updateCard`, `@CacheEvict` on `deleteCard`
- `getAllCards` is NOT cached; private `findCardById` is NOT cached (avoids self-invocation cache issues)

## MongoDB
- Separate package: `com.application.mongo.{controller,service,repository,entity,dto}`
- Entities use String ids (`Product`, `Category`); `ProductService.create/update` validates `categoryId` exists in CategoryRepository
- Mongo endpoints **are** JWT-protected (same filter as Postgres endpoints)

## External jur.лица proxy
- `LegalEntityService` uses `RestClient` with base-url `app.external.jur.base-url` (default `http://localhost:8089`)
- `POST /api/jur` → `POST {base}/jur` (header `proxyInn` = inn from body), returns id
- `GET /api/jur/{id}` → `GET {base}/jur/{id}`
- Request body: `{"inn":"...","ogrn":"..."}` (both required)

## Code conventions
- **Lombok is inconsistent**: `UserMessage` uses `@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`; `User` uses manual getters/setters; Mongo entities use `@Builder`. Follow the pattern of the file you're editing.
- `@Transactional` import: `jakarta.transaction.Transactional` (not Spring's `org.springframework.transaction.annotation.Transactional`)
- DTOs: prefer `record` with `fromEntity()` factory (see `UserDto`, `CardDto`, `TextRequest`, `LegalEntityRequest`)
- Constructor injection (no `@RequiredArgsConstructor` / `@AllArgsConstructor` on services/controllers)

## API
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/login` | no | Login (`{"login":"...","password":"..."}`) → JWT token |
| GET | `/api/users` | yes | List all users |
| GET | `/api/users/{id}` | yes | Get user by id |
| POST | `/api/users` | yes | Create user (`UserDto` body) |
| GET | `/api/cards` | yes | List all cards |
| GET | `/api/cards/{id}` | yes | Get card by id (Redis-cached) |
| POST | `/api/cards` | yes | Create card (`CardDto` body, 201) |
| PUT | `/api/cards/{id}` | yes | Update card (cache-put) |
| DELETE | `/api/cards/{id}` | yes | Delete card (204, cache-evict) |
| POST | `/api/jur` | yes | Proxy create legal entity → external service, returns id |
| GET | `/api/jur/{id}` | yes | Proxy get legal entity |
| GET | `/api/mongo/products` | yes | List products |
| GET | `/api/mongo/products/{id}` | yes | Get product |
| POST | `/api/mongo/products` | yes | Create product (validates categoryId) |
| PUT | `/api/mongo/products/{id}` | yes | Update product |
| DELETE | `/api/mongo/products/{id}` | yes | Delete product (204) |
| GET/POST | `/api/mongo/categories`, `/{id}` | yes | Same CRUD shape as products |
| POST | `/api/text` | no | Send text to `TEST.OUT.TOPIC` (`{"text":"..."}`) |

- Swagger UI: `/swagger-ui.html` (springdoc)

## gRPC (добавлено параллельно с REST, REST не удалён)
- Контракт: `src/main/proto/auth.proto` (сервис `application.auth.v1.AuthService`, rpc `Login`)
- Реализация: `com.application.grpc.auth.AuthGrpcService` (`@Service`, extends `AuthServiceImplBase`) — делегирует в существующий `com.application.service.AuthService`, логика не дублируется
- Нативный gRPC-сервер (Netty) на `spring.grpc.server.port=9090`, поднят Spring Boot 4 стартером `spring-boot-starter-grpc-server` (версия из BOM). Стабы генерируются `io.github.ascopes:protobuf-maven-plugin` (тоже из BOM) в `target/generated-sources/protobuf`
- Reflection включён (`spring.grpc.server.reflection.enabled=true`) — работает grpcurl
- Ошибка логина отдаётся как `Status.INVALID_ARGUMENT` с `withDescription(ex.getMessage())` — тот же текст, что REST отдаёт в `{"message": ...}`
- **Envoy обязателен для браузера**: `envoy.yaml` (docker, upstream `app:9090`) и `envoy.host.yaml` (приложение на хосте, upstream `host.docker.internal:9090`). Слушает `:8081`, фильтры `cors` + `grpc_web` + `router`, маршрут `/grpc/` с `prefix_rewrite: "/"`. В compose добавлен сервис `envoy`
- Локальный запуск приложения на хосте: `docker run -d -p 8081:8081 -p 9901:9901 -v <путь>/envoy.host.yaml:/etc/envoy/envoy.yaml:ro envoyproxy/envoy:v1.34-latest`
- В `envoy.yaml` кластер — `type: STRICT_DNS` (обязательно для hostname, иначе Envoy падает с `malformed IP address`), а не `STATIC`. CORS задан через `typed_per_filter_config` + `CorsPolicy`, HTTP/2 — через `typed_extension_protocol_options`; старые `VirtualHost.cors` и `Cluster.http2_protocol_options` в Envoy 1.34 deprecated

## Auth
- `POST /api/auth/login` returns a JWT token if login+password match an `employees` record
- JWT filter (`FilterConfig` → `JwtAuthFilter`) applies to `/api/users`, `/api/users/*`, `/api/cards`, `/api/cards/*`, `/api/jur`, `/api/jur/*`, `/api/mongo`, `/api/mongo/*`
- `/api/auth` and `/api/text` are open (no token required)
- Token is validated against the `tokens` table in DB; errors return `401` with `{"message":"..."}`
- Before testing protected endpoints, first get a token via `POST /api/auth/login`

## Error response format
All errors return `{"message":"<description>"}` with appropriate HTTP status:
- 400 — business errors (e.g. duplicate email) and validation errors
- 401 — missing/invalid JWT (protected endpoints)
- 404 — resource not found

## Service layer
- `UserService.createUser(UserDto)` — single creation method, invoked from both the REST controller and Kafka listener
- `UserService.getAllUsers()` returns `List<User>` (entities), controller maps to DTOs
- `CardService.getCardDtoById/updateCard` return `CardDto` directly (cached); `getAllCards` returns entities, controller maps
