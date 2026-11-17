# Account Service

A Java 21 / Spring Boot REST service for maintaining account balances and transaction histories. It persists account and event entities through Spring Data JPA, currently using an in-memory H2 database.

## Architecture

```text
HTTP client
    |
    v
AccountController ------> ApiExceptionHandler
    |
    v
AccountService
    |
    v
AccountRepository (Spring Data JPA)
    |
    v
Account / Event entities -----> H2 database

EventGatewayClient (OpenFeign) -----> Event Gateway
       |                                  |
       +---- circuit breaker,             +---- configured by event-gateway.url
             semaphore bulkhead,
             fallback
```

### Components

- **Controller** (`controller/AccountController`) exposes account transaction, balance, and detail endpoints.
- **Service** (`service/AccountService`) creates accounts on the first transaction, applies credits and debits, skips an event already recorded for that account, and returns transaction history sorted by timestamp.
- **Repository** (`repo/AccountRepository`) is a Spring Data JPA repository keyed by account ID.
- **Entities** (`entity/Account`, `entity/Event`) represent accounts and their transaction events. Account-to-event persistence uses cascade operations.
- **DTOs** (`dto`) define request and response payloads. Transaction requests require a non-null event ID, amount, type, and timestamp; amounts must be at least `0.01`.
- **Event Gateway client** (`feignClient/EventGatewayClient`) defines Feign lookups for one event or all events for an account. OpenFeign circuit-breaker integration is enabled, with a semaphore bulkhead configured for up to 10 concurrent calls and no wait. Its fallback raises an unavailable-service exception.
- **Global exception handler** (`controller/ApiExceptionHandler`) converts known request, database, and dependency failures to a JSON error response. It returns 503 for dependency/database connectivity failures, 409 for data integrity conflicts, 400 for invalid requests, and 500 for other database or unexpected errors.

The current account controller and service do not call `EventGatewayClient`; the Feign integration and its resilience behavior are available but are not part of the account request flow yet.

## API

The application listens on port `8080` by default.

| Method | Path | Description |
|---|---|---|
| `GET` | `/health` | Return aggregate application health as `{"status":"UP"}` with `200 OK`, or a non-UP status with `503 Service Unavailable`. |
| `POST` | `/accounts/{accountId}/transactions` | Apply a credit or debit transaction. Returns `200 OK` with an empty body on success. |
| `GET` | `/accounts/{accountId}/balance` | Return the balance. An account that does not exist currently returns a zero balance. |
| `GET` | `/accounts/{accountId}` | Return account balance and timestamp-sorted transactions, or `404 Not Found` if the account does not exist. |

Example transaction:

```http
POST /accounts/acct-123/transactions
Content-Type: application/json

{
  "eventId": "evt-001",
  "amount": 25.50,
  "type": "CREDIT",
  "eventTimestamp": "2025-01-02T03:04:05Z"
}
```

Use `CREDIT` or `DEBIT` for the transaction type. The current validation only requires the type to be non-null; it does not reject other strings. Such an unrecognized type is recorded but does not change the balance.

Example balance response:

```json
{
  "accountId": "acct-123",
  "balance": 25.50
}
```

Error responses handled by the global advice have this shape:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed."
}
```

The account-details not-found response is currently generated directly by the controller and has an empty `404` body.

The `/health` endpoint uses Spring Boot Actuator's aggregate health status and exposes only the overall status, not individual component details.

## Prerequisites

- JDK 21
- No separate Maven installation is needed; the Maven Wrapper is included.

Check the Java installation:

```shell
java -version
```

## Run locally

From the repository root, start the service with the Maven Wrapper:

```shell
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

Alternatively, build the executable JAR and run it:

```shell
# Windows PowerShell
.\mvnw.cmd package
java -jar target/AccountService-0.0.1-SNAPSHOT.jar

# macOS / Linux
./mvnw package
java -jar target/AccountService-0.0.1-SNAPSHOT.jar
```

The default H2 JDBC URL is `jdbc:h2:mem:accountservice`. It is in-memory, so account data is lost when the process stops. The configured Event Gateway URL is `http://localhost:8800`.

Override the gateway URL or HTTP port when starting the application, for example:

```shell
java -jar target/AccountService-0.0.1-SNAPSHOT.jar \
  --event-gateway.url=http://localhost:8800 \
  --server.port=8081
```

## Tests and coverage

Run the test suite:

```shell
# Windows PowerShell
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

Run verification, generate the JaCoCo report, and enforce the configured 100% line-coverage minimum:

```shell
# Windows PowerShell
.\mvnw.cmd verify

# macOS / Linux
./mvnw verify
```

The HTML coverage report is generated at `target/site/jacoco/index.html`.

## Configuration and operational notes

Application defaults are in `src/main/resources/application.yaml`. The gateway URL uses `event-gateway.url`; the default server port is Spring Boot's `8080`.

The YAML currently places some settings under unexpected parents: JPA and H2-console options are nested below `spring.datasource`, while Actuator options are below `spring.management`. Spring Boot normally expects these under `spring.jpa`, `spring.h2.console`, and top-level `management`. Likewise, the `timelimiter`, `retry`, and `ratelimiter` sections are nested under `spring.datasource`. Do not assume those options are active without correcting the property paths and checking the application startup/configuration. The `spring.cloud.openfeign.circuitbreaker` and root `resilience4j` settings are separately configured.

No separate migration tool or production database profile is currently defined. Before production use, choose a persistent database, configure credentials and schema migrations, and review which Actuator endpoints and H2 console (if enabled) should be exposed.
