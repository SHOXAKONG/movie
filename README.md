# Movie Reservation System

A backend for the [roadmap.sh Movie Reservation System](https://roadmap.sh/projects/movie-reservation-system) project.
It uses Spring Boot 4, Java 21, PostgreSQL and Liquibase, and is structured with Clean Architecture
(see [src/ARCHITECTURE.md](src/ARCHITECTURE.md)).

## Run it

Requires Docker (Spring Boot starts PostgreSQL from `compose.yaml` automatically).

```bash
./mvnw spring-boot:run
```

- API docs and a test UI: http://localhost:8080/swagger-ui.html (click **Authorize** and paste a token)
- `./mvnw spring-boot:run` uses the `dev` profile ([application-dev.yaml](src/main/resources/application-dev.yaml)), which seeds the admin `admin@movie.local` / `admin12345`. In IntelliJ, use the shared **MovieApplication (dev)** run configuration (from `.run/`), or set the active profile to `dev` yourself. To make every local run use `dev` automatically, create a git-ignored `config/application.yaml` in the project root containing `spring.profiles.active: dev`; Spring Boot reads it when the app starts from that folder.
- In any other environment there are no fallback values. Set:
  - `JWT_SECRET` (required, at least 32 characters; the app won't start without it)
  - `ADMIN_EMAIL` and `ADMIN_PASSWORD` to seed the first admin
- Two auditoriums are seeded: Hall 1 (8×12) and Hall 2 (6×10).

Tests (needs Docker for the end-to-end test):

```bash
./mvnw test
```

## API

Write requests take a JSON body. Errors use [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem details:
400 for invalid input, 401, 403, 404, and 409 for seat or schedule conflicts.

| Who | Method & path | Purpose |
|---|---|---|
| Public | `POST /api/auth/register` | `{email, password, fullName}` |
| Public | `POST /api/auth/login` | `{email, password}` → `{accessToken, expiresAt, user}` |
| User | `GET /api/users/me` | Current user |
| Public | `GET /api/movies?genre=Drama` | List movies (optional genre filter) |
| Public | `GET /api/movies/{id}` | One movie |
| Public | `GET /api/showtimes?date=2026-11-01` | Movies with their bookable showtimes on a date |
| Public | `GET /api/showtimes/{id}` | One showtime with available seat count |
| Public | `GET /api/showtimes/{id}/seats` | Seat map grouped by row, with availability |
| User | `POST /api/reservations` | `{showtimeId, seats: ["A1","A2"]}` |
| User | `GET /api/reservations` | My reservations |
| User | `POST /api/reservations/{id}/cancel` | Cancel my upcoming reservation |
| Admin | `POST /api/admin/movies` | `{title, description, posterUrl, durationMinutes, genres: []}` |
| Admin | `PUT` / `DELETE /api/admin/movies/{id}` | Update / delete a movie |
| Admin | `GET` / `POST /api/admin/auditoriums` | `{name, rows, seatsPerRow}` |
| Admin | `POST /api/admin/showtimes` | `{movieId, auditoriumId, startTime: "2026-11-01T18:00", price}` |
| Admin | `DELETE /api/admin/showtimes/{id}` | Delete (refused while it has confirmed reservations) |
| Admin | `POST /api/admin/users/{id}/promote` | Make a user an admin |
| Admin | `GET /api/admin/reservations?showtimeId=&from=&to=` | All reservations |
| Admin | `GET /api/admin/reports/occupancy?from=&to=` | Capacity, reserved and available seats, occupancy % per showtime |
| Admin | `GET /api/admin/reports/revenue?from=&to=` | Total revenue plus a per-movie breakdown |

Report date ranges default to 30 days before and after today.

## Business rules

- A showtime blocks its hall for the movie's runtime plus 15 minutes of cleanup. Overlapping showtimes are rejected, and the database enforces this too.
- A seat can belong to only one active reservation per showtime. A PostgreSQL partial unique index guarantees this even under concurrent requests.
- 1–10 seats per reservation. Price = showtime price × number of seats.
- Users can cancel only their own reservations, and only before the showtime starts. Cancelling frees the seats.
- Movies and showtimes with confirmed reservations cannot be deleted, which keeps revenue history intact. The database enforces this too; cancelled reservations are removed along with them.
- A movie's duration cannot change while it has showtimes that haven't finished.
- Emails are unique, and auditorium names are unique ignoring case. Duplicates return 409.
