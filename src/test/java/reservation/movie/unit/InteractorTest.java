package reservation.movie.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reservation.movie.entities.Auditorium;
import reservation.movie.entities.DomainException;
import reservation.movie.entities.User;
import reservation.movie.usecases.exceptions.AuthenticationFailedException;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.exceptions.ForbiddenException;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.exceptions.SeatsAlreadyReservedException;
import reservation.movie.usecases.interactors.AuthInteractor;
import reservation.movie.usecases.interactors.MovieInteractor;
import reservation.movie.usecases.interactors.ReservationInteractor;
import reservation.movie.usecases.interactors.ShowtimeInteractor;
import reservation.movie.usecases.ports.input.auth.LoginRequest;
import reservation.movie.usecases.ports.input.auth.RegisterRequest;
import reservation.movie.usecases.ports.input.movie.MovieRequest;
import reservation.movie.usecases.ports.input.reservation.ReservationResponse;
import reservation.movie.usecases.ports.input.reservation.ReserveSeatsRequest;
import reservation.movie.usecases.ports.input.showtime.ScheduleShowtimeRequest;
import reservation.movie.usecases.ports.input.showtime.ShowtimeResponse;
import reservation.movie.usecases.ports.output.auth.IPasswordHasher;
import reservation.movie.usecases.ports.output.auth.ITokenIssuer;
import reservation.movie.usecases.ports.output.transaction.ITransactionRunner;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InteractorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 12, 0);
    private final Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    private final InMemoryRepositories.Movies movies = new InMemoryRepositories.Movies();
    private final InMemoryRepositories.Auditoriums auditoriums = new InMemoryRepositories.Auditoriums();
    private final InMemoryRepositories.Showtimes showtimes = new InMemoryRepositories.Showtimes();
    private final InMemoryRepositories.Reservations reservations = new InMemoryRepositories.Reservations(showtimes);

    private final IPasswordHasher hasher = new IPasswordHasher() {
        @Override
        public String hash(String raw) {
            return "hashed:" + raw;
        }

        @Override
        public boolean matches(String raw, String hash) {
            return hash.equals("hashed:" + raw);
        }
    };
    private final ITokenIssuer tokens = user -> new ITokenIssuer.IssuedToken("token-" + user.getId(), Instant.EPOCH);

    private final ITransactionRunner transactions = Runnable::run;

    private final AuthInteractor auth = new AuthInteractor(users, hasher, tokens);
    private final MovieInteractor movieUseCases =
            new MovieInteractor(movies, showtimes, reservations, transactions, clock);
    private final ShowtimeInteractor showtimeUseCases =
            new ShowtimeInteractor(showtimes, movies, auditoriums, reservations, transactions, clock);
    private final ReservationInteractor reservationUseCases =
            new ReservationInteractor(reservations, showtimes, movies, auditoriums, clock);

    private Long showtimeId;

    @BeforeEach
    void setUp() {
        Long hallId = auditoriums.save(Auditorium.create("Hall 1", 4, 5)).getId();
        Long movieId = movieUseCases.create(new MovieRequest("Heat", null, null, 170, List.of("Crime"))).id();
        showtimeId = showtimeUseCases.schedule(
                new ScheduleShowtimeRequest(movieId, hallId, NOW.plusDays(1), new BigDecimal("10"))).id();
    }

    @Test
    void registerRejectsDuplicateEmailsAndShortPasswords() {
        auth.register(new RegisterRequest("Ann@Example.com", "password1", "Ann"));

        assertThatThrownBy(() -> auth.register(new RegisterRequest("ann@example.com", "password2", "Ann")))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> auth.register(new RegisterRequest("bob@example.com", "short", "Bob")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void loginChecksPassword() {
        auth.register(new RegisterRequest("ann@example.com", "password1", "Ann"));

        assertThat(auth.login(new LoginRequest("ANN@example.com", "password1")).accessToken()).startsWith("token-");
        assertThatThrownBy(() -> auth.login(new LoginRequest("ann@example.com", "wrong-pass")))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void onlyAdminsCanPromote() {
        Long ann = auth.register(new RegisterRequest("ann@example.com", "password1", "Ann")).id();
        Long bob = auth.register(new RegisterRequest("bob@example.com", "password1", "Bob")).id();
        auth.ensureAdminExists("root@example.com", "password1", "Root");
        Long admin = users.findByEmail("root@example.com").map(User::getId).orElseThrow();

        assertThatThrownBy(() -> auth.promoteToAdmin(ann, bob)).isInstanceOf(ForbiddenException.class);
        assertThat(auth.promoteToAdmin(admin, bob).role()).isEqualTo("ADMIN");
    }

    @Test
    void overlappingShowtimeIsRejected() {
        ShowtimeResponse existing = showtimeUseCases.get(showtimeId);

        assertThatThrownBy(() -> showtimeUseCases.schedule(new ScheduleShowtimeRequest(existing.movieId(),
                existing.auditoriumId(), NOW.plusDays(1).plusHours(1), BigDecimal.TEN)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("busy");
    }

    @Test
    void takenSeatsCannotBeReservedAgain() {
        reservationUseCases.reserve(1L, new ReserveSeatsRequest(showtimeId, List.of("A1", "A2")));

        assertThatThrownBy(() -> reservationUseCases.reserve(2L, new ReserveSeatsRequest(showtimeId, List.of("A2", "A3"))))
                .isInstanceOf(SeatsAlreadyReservedException.class)
                .hasMessageContaining("A2");
        assertThat(showtimeUseCases.seatMap(showtimeId).availableSeats()).isEqualTo(18);
    }

    @Test
    void cancellingFreesSeatsAndIsOwnerOnly() {
        ReservationResponse r = reservationUseCases.reserve(1L, new ReserveSeatsRequest(showtimeId, List.of("B1")));

        assertThatThrownBy(() -> reservationUseCases.cancel(2L, r.id())).isInstanceOf(NotFoundException.class);
        assertThat(reservationUseCases.cancel(1L, r.id()).status()).isEqualTo("CANCELLED");
        assertThat(reservationUseCases.reserve(2L, new ReserveSeatsRequest(showtimeId, List.of("B1"))).seats())
                .containsExactly("B1");
    }

    @Test
    void movieWithConfirmedReservationsCannotBeDeleted() {
        Long movieId = showtimeUseCases.get(showtimeId).movieId();
        reservationUseCases.reserve(1L, new ReserveSeatsRequest(showtimeId, List.of("C3")));

        assertThatThrownBy(() -> movieUseCases.delete(movieId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void scheduleForDateGroupsShowtimesByMovie() {
        var schedule = showtimeUseCases.listForDate(NOW.toLocalDate().plusDays(1));

        assertThat(schedule).hasSize(1);
        assertThat(schedule.getFirst().movie().title()).isEqualTo("Heat");
        assertThat(schedule.getFirst().showtimes()).singleElement()
                .satisfies(s -> assertThat(s.availableSeats()).isEqualTo(20));
        assertThat(showtimeUseCases.listForDate(NOW.toLocalDate())).isEmpty();
    }

    @Test
    void movieDurationIsFrozenWhileItHasUpcomingShowtimes() {
        Long movieId = showtimeUseCases.get(showtimeId).movieId();

        assertThatThrownBy(() -> movieUseCases.update(movieId, new MovieRequest("Heat", null, null, 200, List.of("Crime"))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("duration");
        assertThat(movieUseCases.update(movieId, new MovieRequest("Heat (1995)", null, null, 170, List.of("Crime"))).title())
                .isEqualTo("Heat (1995)");
    }

    @Test
    void movieDurationCanChangeWhenItHasNoUpcomingShowtimes() {
        Long movieId = movieUseCases.create(new MovieRequest("Alien", null, null, 117, List.of("Horror"))).id();

        assertThat(movieUseCases.update(movieId, new MovieRequest("Alien", null, null, 116, List.of("Horror")))
                .durationMinutes()).isEqualTo(116);
    }

    @Test
    void deletingAShowtimeRemovesItsCancelledReservations() {
        ReservationResponse r = reservationUseCases.reserve(1L, new ReserveSeatsRequest(showtimeId, List.of("D4")));
        reservationUseCases.cancel(1L, r.id());

        showtimeUseCases.delete(showtimeId);

        assertThat(showtimes.findById(showtimeId)).isEmpty();
        assertThat(reservations.findById(r.id())).isEmpty();
    }

    @Test
    void deleteRunsInsideOneTransaction() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        ITransactionRunner counting = work -> {
            calls.incrementAndGet();
            work.run();
        };
        Long movieId = showtimeUseCases.get(showtimeId).movieId();

        new ShowtimeInteractor(showtimes, movies, auditoriums, reservations, counting, clock).delete(showtimeId);
        new MovieInteractor(movies, showtimes, reservations, counting, clock).delete(movieId);

        assertThat(calls).hasValue(2);
    }
}
