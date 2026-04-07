# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Description

DietiEstates25 is a real estate management platform. Features: property search/creation, visit booking, offer management, geospatial integration (Geoapify), weather forecasts (Open Meteo), and a multi-tenant agency system with role-based access.

## Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.0.5
- **Database:** PostgreSQL 16
- **Auth:** JWT via Spring Security OAuth2 Resource Server (no AWS Cognito)
- **Build:** Maven 3.9.13

## Commands

All Maven commands run from `backend/`:

```bash
# Build
mvn clean package -DskipTests

# Run all tests
mvn clean test

# Run a single test class
mvn clean test -Dtest=AuthServiceSecurityTests

# Run a single test method
mvn clean test -Dtest=AuthServiceSecurityTests#testPasswordHashingNotPlaintext

# Run tests by package pattern
mvn clean test -Dtest="com.dietiestate25backend.security.**"

# Generate JaCoCo coverage report (output: target/site/jacoco/index.html)
mvn clean test jacoco:report

# Run the application
mvn spring-boot:run
```

Full integration tests require PostgreSQL via Docker (from project root):

```bash
docker compose -f compose.test.yaml up --abort-on-container-exit
```

Tests use `SPRING_PROFILES_ACTIVE=test` and connect to a PostgreSQL container defined in `compose.test.yaml`.

## Architecture

Layered architecture (by technical layer), main package: `com.dietiestate25backend`.

```
controller/         → HTTP input layer; parses and validates requests
service/            → Business logic; orchestrates domain operations
dao/
  modelinterface/   → DAO interfaces (contracts)
  postgresimplements/ → JdbcTemplate implementations
  externalimplements/ → External API integrations (Geoapify, Open Meteo)
model/              → Domain entities (Lombok @Getter/@RequiredArgsConstructor)
dto/
  requests/         → Inbound DTOs with Jakarta validation annotations
  response/         → Outbound DTOs
error/
  exception/        → Custom exceptions (BadRequestException, UnauthorizedException, etc.)
  GlobalExceptionHandler.java → @ControllerAdvice; single error translation point
utils/
  SecurityConfig.java → JWT OAuth2 resource server config, public/protected route definitions
  TokenUtils.java     → JWT claim extraction, role authorization helpers
```

## Key Architectural Decisions

**DAO pattern:** All data access goes through an interface in `modelinterface/`. The PostgreSQL implementation uses `JdbcTemplate` with raw SQL. Never use Spring Data JPA — the project intentionally uses Spring Data JDBC / JdbcTemplate.

**Error handling:** All exceptions must extend the custom hierarchy in `error/exception/`. `GlobalExceptionHandler` is the sole place where exceptions become HTTP responses. `ErrorCode.java` centralizes all error codes.

**Security:** Stateless JWT. `SecurityConfig` defines public endpoints (`/auth/login`, `/auth/register`, `/immobile/cerca`) and everything else requires a valid token. `TokenUtils` extracts claims from the `SecurityContext` — use it for authorization checks in services.

**Roles:** `Admin`, `Gestore`, `AgenteImmobiliare`, `Cliente`. Agency staff (Admin, Gestore, Agente) are linked to an agency via the `utenteagenzia` join table.

**Account locking:** `Utente` tracks `failedLoginAttempts` and `lockedUntil`; `AuthService` enforces brute-force lockout.

## Testing

Tests live in `src/test/java/com/dietiestate25backend/` organized by concern (`security/auth/`, `security/business/`, `security/dao/`).

- `BaseMvcTest` — base class for controller-layer tests using `MockMvc` + `@ActiveProfiles("test")`
- `BaseIntegrationTest` — base class for tests that hit a real database
- Unit tests use `@ExtendWith(MockitoExtension.class)` with `@Mock` / `@InjectMocks`

Per the project guidelines: **no unit tests for controllers or repositories**. Unit tests target services and domain type invariants only.

## Database

Schema in `db-init/01_schema.sql`, test fixtures in `db-init/02_test_data.sql`. Key tables: `utenti`, `agenzia`, `utenteagenzia`, `immobile`, `offerta`, `visita`. PostgreSQL enums: `statoofferta` and `statovisita` (`In Sospeso`, `Accettata`/`Confermata`, `Rifiutata`).

## Functional Requirements

- Authentication (register/login for clients and agency staff, JWT issuance)
- Property management (create, search with filters, list personal properties)
- Visit booking with email notification
- Offer lifecycle management
- Geospatial data (GPS coordinates, POIs via Geoapify)
- Weather forecasts via Open Meteo

## Non-Functional Requirements

- Role-based access control enforced at service layer via JWT claims
- Brute-force protection: account lockout after repeated failed logins
- Stateless sessions (no server-side session state)
- Structured test coverage tracked via JaCoCo + SonarQube (CI in `.github/workflows/`)
