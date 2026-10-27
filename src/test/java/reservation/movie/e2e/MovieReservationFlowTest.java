package reservation.movie.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reservation.movie.adapters.controllers.ReservationController;
import reservation.movie.adapters.controllers.request.ReserveSeatsHttpRequest;
import reservation.movie.main.MovieApplication;
import reservation.movie.usecases.exceptions.SeatsAlreadyReservedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the whole application against a real PostgreSQL (Testcontainers), so the Liquibase schema,
 * the no-overlap constraint and the no-double-booking index are exercised for real.
 */
@SpringBootTest(classes = MovieApplication.class, properties = {
        "app.jwt.secret=e2e-test-secret-0123456789abcdef-0123",
        "app.admin.email=admin@test.local",
        "app.admin.password=admin-password",
        "spring.docker.compose.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class MovieReservationFlowTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    MockMvc mvc;

    @Autowired
    ReservationController reservationController;

    @Test
    void fullReservationLifecycle() throws Exception {
        String admin = login("admin@test.local", "admin-password");
        String alice = registerAndLogin();
        String bob = registerAndLogin();

        // Regular users cannot manage movies; anonymous users cannot reserve.
        call(post("/api/admin/movies"), alice, movieJson("Nope")).andExpect(status().isForbidden());
        call(post("/api/reservations"), null, "{}").andExpect(status().isUnauthorized());

        long movieId = id(call(post("/api/admin/movies"), admin, movieJson("Interstellar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duration").value("2h 49m")));
        long hallId = ((Number) JsonPath.read(body(call(get("/api/admin/auditoriums"), admin, null)), "$[0].id"))
                .longValue();

        LocalDate day = LocalDate.now().plusDays(3);
        long showtimeId = id(call(post("/api/admin/showtimes"), admin, showtimeJson(movieId, hallId, day + "T18:00"))
                .andExpect(status().isCreated()));
        call(post("/api/admin/showtimes"), admin, showtimeJson(movieId, hallId, day + "T19:30"))
                .andExpect(status().isConflict());

        // Browse movies and seats for a date (public).
        call(get("/api/showtimes?date=" + day), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.movie.id == %d)].showtimes[0].availableSeats".formatted(movieId))
                        .value(96));

        // Reserve, then a clash on one seat.
        long reservationId = id(call(post("/api/reservations"), alice, reserveJson(showtimeId, "A1", "A2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(25.0))
                .andExpect(jsonPath("$.seats", hasSize(2))));
        call(post("/api/reservations"), bob, reserveJson(showtimeId, "A2", "A3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Seats already reserved: A2"));
        call(get("/api/showtimes/" + showtimeId + "/seats"), null, null)
                .andExpect(jsonPath("$.availableSeats").value(94))
                .andExpect(jsonPath("$.rows[0].seats[0].available").value(false));

        call(get("/api/reservations"), alice, null).andExpect(jsonPath("$", hasSize(1)));
        call(get("/api/reservations"), bob, null).andExpect(jsonPath("$", hasSize(0)));

        // Admin reporting.
        String range = "?from=" + day + "&to=" + day;
        call(get("/api/admin/reports/occupancy" + range), admin, null)
                .andExpect(jsonPath("$[?(@.showtimeId == %d)].reservedSeats".formatted(showtimeId)).value(2));
        call(get("/api/admin/reports/revenue" + range), admin, null)
                .andExpect(jsonPath("$.totalRevenue").value(25.0))
                .andExpect(jsonPath("$.totalSeatsSold").value(2));
        call(get("/api/admin/reservations?showtimeId=" + showtimeId), admin, null)
                .andExpect(jsonPath("$", hasSize(1)));
        call(get("/api/admin/reports/revenue"), alice, null).andExpect(status().isForbidden());

        // Movies with confirmed reservations are protected.
        call(delete("/api/admin/movies/" + movieId), admin, null).andExpect(status().isConflict());

        // Only the owner can cancel; cancelling frees the seats.
        call(post("/api/reservations/" + reservationId + "/cancel"), bob, null).andExpect(status().isNotFound());
        call(post("/api/reservations/" + reservationId + "/cancel"), alice, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        call(post("/api/reservations"), bob, reserveJson(showtimeId, "A2"))
                .andExpect(status().isCreated());
        call(get("/api/admin/reports/revenue" + range), admin, null)
                .andExpect(jsonPath("$.totalRevenue").value(12.5));
    }

    @Test
    void adminCanPromoteUsers() throws Exception {
        String admin = login("admin@test.local", "admin-password");
        String email = "carol-" + UUID.randomUUID() + "@test.local";
        long carolId = id(call(post("/api/auth/register"), null,
                "{\"email\":\"%s\",\"password\":\"password123\",\"fullName\":\"Carol\"}".formatted(email))
                .andExpect(status().isCreated()));
        String carol = login(email, "password123");

        call(get("/api/admin/auditoriums"), carol, null).andExpect(status().isForbidden());
        call(post("/api/admin/users/" + carolId + "/promote"), admin, null)
                .andExpect(jsonPath("$.role").value("ADMIN"));
        call(get("/api/admin/auditoriums"), login(email, "password123"), null).andExpect(status().isOk());
    }

    @Test
    void concurrentBookingsOfTheSameSeatProduceExactlyOneReservation() throws Exception {
        String admin = login("admin@test.local", "admin-password");
        long movieId = id(call(post("/api/admin/movies"), admin, movieJson("Race")));
        long hallId = id(call(post("/api/admin/auditoriums"), admin,
                "{\"name\":\"Race Hall %s\",\"rows\":2,\"seatsPerRow\":2}".formatted(UUID.randomUUID())));
        long showtimeId = id(call(post("/api/admin/showtimes"), admin,
                showtimeJson(movieId, hallId, LocalDate.now().plusDays(5) + "T20:00")));

        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        int successes = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                long userId = id(call(post("/api/auth/register"), null,
                        "{\"email\":\"racer-%s@test.local\",\"password\":\"password123\"}".formatted(UUID.randomUUID())));
                Callable<Boolean> attempt = () -> {
                    start.await();
                    try {
                        reservationController.reserve(userId, new ReserveSeatsHttpRequest(showtimeId, List.of("B2")));
                        return true;
                    } catch (SeatsAlreadyReservedException e) {
                        return false;
                    }
                };
                results.add(pool.submit(attempt));
            }
            start.countDown();
            for (Future<Boolean> result : results) {
                if (result.get()) {
                    successes++;
                }
            }
        }

        assertThat(successes).isEqualTo(1);
        call(get("/api/showtimes/" + showtimeId + "/seats"), null, null)
                .andExpect(jsonPath("$.availableSeats").value(3));
    }

    // ---- helpers ----

    private String registerAndLogin() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.local";
        call(post("/api/auth/register"), null,
                "{\"email\":\"%s\",\"password\":\"password123\",\"fullName\":\"Test User\"}".formatted(email))
                .andExpect(status().isCreated());
        return login(email, "password123");
    }

    private String login(String email, String password) throws Exception {
        String json = body(call(post("/api/auth/login"), null,
                "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)).andExpect(status().isOk()));
        return JsonPath.read(json, "$.accessToken");
    }

    private ResultActions call(MockHttpServletRequestBuilder request, String token, String json) throws Exception {
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mvc.perform(request);
    }

    private static String body(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    private static long id(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(body(result), "$.id")).longValue();
    }

    private static String movieJson(String title) {
        return """
                {"title":"%s","description":"A test movie","posterUrl":"https://img.example.com/p.jpg",
                 "durationMinutes":169,"genres":["Sci-Fi","Drama"]}""".formatted(title);
    }

    private static String showtimeJson(long movieId, long hallId, String start) {
        return "{\"movieId\":%d,\"auditoriumId\":%d,\"startTime\":\"%s\",\"price\":12.50}"
                .formatted(movieId, hallId, start);
    }

    private static String reserveJson(long showtimeId, String... seats) {
        return "{\"showtimeId\":%d,\"seats\":[%s]}".formatted(showtimeId,
                String.join(",", java.util.Arrays.stream(seats).map(s -> "\"" + s + "\"").toList()));
    }
}
