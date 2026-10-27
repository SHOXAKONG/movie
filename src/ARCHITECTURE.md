# Movie Reservation — Clean Architecture

This service follows Robert C. Martin's *Clean Architecture*. The package tree is organised
around the movie reservation domain, and every source dependency points **inwards**.

```
main ──► frameworks ──► adapters ──► usecases ──► entities
```

## Layers (`src/main/java/reservation/movie/`)

| Package | Layer | Contents |
|---|---|---|
| `entities` | 1 · Enterprise rules | `Movie`, `Auditorium`, `Seat`, `Showtime`, `Reservation`, `User`, `Role`, `DomainException`. Plain Java. |
| `usecases.ports.input.<feature>` | 2 · Application rules | One input port per feature (`auth`, `movie`, `auditorium`, `showtime`, `reservation`, `report`), with its request/response records next to it |
| `usecases.ports.output.<feature>` | 2 | Repository ports per feature; `auth` also holds `IPasswordHasher` and `ITokenIssuer`; `report` holds the report read models |
| `usecases.interactors` | 2 | One interactor per input port |
| `usecases.exceptions` | 2 | `NotFoundException`, `ConflictException`, `SeatsAlreadyReservedException`, `ForbiddenException`, `AuthenticationFailedException` |
| `adapters.controllers` | 3 · Interface adapters | Call input ports and pass results through presenters |
| `adapters.presenters.<feature>` | 3 | Presenter + view models per feature (formatted duration, seat map grouped by row, `soldOut`, token type) |
| `adapters.gateways.<feature>` | 3 | Per feature: `*Gateway` (implements the repository port), `*DataSource` interface for the database, `*Record` |
| `frameworks.db.entity` | 4 · Frameworks & drivers | JPA `@Entity` classes (`*JpaEntity`) |
| `frameworks.db.repository` | 4 | Spring Data repositories, including the native report SQL (`ReportQueryRepository`) |
| `frameworks.db.datasource` | 4 | `Jpa*DataSource`: implement the adapters' `*DataSource` interfaces and translate constraint violations |
| `frameworks.security` | 4 | JWT (HS256) encode/decode, URL rules, BCrypt, `AdminSeeder` |
| `frameworks.web` | 4 | `@RestController`s, `ApiExceptionHandler` (RFC 9457 problem details), OpenAPI config |
| `frameworks.external` | 4 | Placeholder for third-party clients (payments, email) |
| `main` | Composition root | `MovieApplication`, `MovieConfiguration` (creates every gateway, interactor, presenter and controller), `PersistenceConfiguration` |

Schema: Liquibase changesets in `src/main/resources/db/changelog`. Hibernate only validates the schema.

### One deliberate simplification

Input ports **return** a response model instead of pushing it into an Output Port. This keeps
presenters stateless, so no request-scoped beans are needed. Presenters still own every
display decision, and the interactors still know nothing about HTTP.

## How the hard rules are enforced

| Rule | Business check (fast, readable error) | Database guarantee (safe under concurrency) |
|---|---|---|
| No double booking | `ReservationInteractor` rejects seats already held | Partial unique index `ux_reservation_seats_active (showtime_id, seat_row, seat_number) WHERE active` → `SeatsAlreadyReservedException` → 409 |
| No overlapping showtimes in one hall | `ShowtimeInteractor` queries for overlaps (runtime + 15 min cleanup) | `EXCLUDE USING gist (auditorium_id WITH =, tsrange(start_time, end_time) WITH &&)` → 409 |
| Cancel only upcoming, own reservations | `Reservation.cancel` + ownership check (others get 404) | — |
| Only admins manage/report/promote | `/api/admin/**` requires `ROLE_ADMIN`; `promoteToAdmin` re-checks the actor | — |
| Movie with confirmed reservations can't be deleted | `MovieInteractor.delete` | FK constraints |

## Request flow example: `POST /api/reservations`

1. `frameworks.web.ReservationRestController` reads the user id from the JWT and calls `adapters.controllers.ReservationController`.
2. `ReservationInteractor.reserve`: loads the showtime and auditorium, parses the seats, applies `Reservation.create` (seat exists, max 10, not started, price × seats), and rejects seats that are already held.
3. `ReservationGateway` → `ReservationDataSource` → `JpaReservationDataSource` inserts the rows. The unique index catches any race.
4. `ReservationPresenter` turns the response into a `ReservationViewModel`, which is returned as JSON.

## Adding a feature

1. **Entity:** add the rules to `entities`.
2. **Ports:** add a request/response record and an input port method; add repository methods if needed.
3. **Interactor:** implement it, and unit-test it with the fakes in `InMemoryRepositories`.
4. **Adapters:** add a presenter/view model, a controller method, and gateway + data source methods.
5. **Frameworks:** add the REST endpoint, the JPA query and a Liquibase changeset.
6. **Main:** wire any new objects in `MovieConfiguration`.

## Tests (`src/test/java/reservation/movie/`)

| Folder | What |
|---|---|
| `unit` | Entity rules and interactors with in-memory fakes, no Spring |
| `e2e` | Whole app + real PostgreSQL via Testcontainers: full lifecycle, promotion, 8-thread double-booking race |
| `architecture` | ArchUnit: the Dependency Rule, plus "no Spring/JPA/Lombok inside entities, usecases or adapters" |
