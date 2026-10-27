package reservation.movie.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reservation.movie.adapters.controllers.MovieController;
import reservation.movie.entities.Auditorium;
import reservation.movie.entities.Movie;
import reservation.movie.entities.Reservation;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;
import reservation.movie.entities.User;
import reservation.movie.main.MovieApplication;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.auth.IUserRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;
import reservation.movie.usecases.ports.output.transaction.ITransactionRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks the guarantees the database gives on its own, i.e. when a race lets a request slip past the
 * use case's checks. Repository ports are called directly to simulate exactly that.
 */
@SpringBootTest(classes = MovieApplication.class, properties = {
        "app.jwt.secret=integration-test-secret-0123456789abcdef",
        "spring.docker.compose.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class PersistenceGuardsIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired IUserRepository users;
    @Autowired IMovieRepository movies;
    @Autowired IAuditoriumRepository auditoriums;
    @Autowired IShowtimeRepository showtimes;
    @Autowired IReservationRepository reservations;
    @Autowired ITransactionRunner transactions;
    @Autowired MovieController movieController;

    private Long userId;
    private Movie movie;
    private Showtime showtime;
    private Auditorium hall;

    @BeforeEach
    void setUp() {
        userId = users.save(User.register("guard-" + UUID.randomUUID() + "@test.local", "hash", "Guard")).getId();
        movie = movies.save(Movie.create("Guarded " + UUID.randomUUID(), null, null, 100, List.of("Drama")));
        hall = auditoriums.save(Auditorium.create("Guard Hall " + UUID.randomUUID(), 3, 3));
        LocalDateTime now = LocalDateTime.now();
        showtime = showtimes.save(Showtime.schedule(movie, hall, now.plusDays(2), BigDecimal.TEN, now));
    }

    private Reservation book(String seat) {
        return reservations.save(Reservation.create(userId, showtime, hall, List.of(Seat.parse(seat)), LocalDateTime.now()));
    }

    @Test
    void duplicateEmailThatSlipsPastTheCheckIsAConflict() {
        String email = "twice-" + UUID.randomUUID() + "@test.local";
        users.save(User.register(email, "hash", "First"));

        assertThatThrownBy(() -> users.save(User.register(email, "hash", "Second")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");
    }

    @Test
    void auditoriumNamesAreUniqueIgnoringCase() {
        assertThatThrownBy(() -> auditoriums.save(Auditorium.create(hall.getName().toUpperCase(), 2, 2)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void showtimeDeleteThatSlipsPastTheCheckKeepsConfirmedAndCancelledReservations() {
        Reservation confirmed = book("A1");
        Reservation cancelled = reservations.save(book("A2").cancel(showtime, LocalDateTime.now()));

        // Same steps the use case runs after its check, as if a booking arrived in between.
        assertThatThrownBy(() -> transactions.run(() -> {
            reservations.deleteCancelledForShowtime(showtime.getId());
            showtimes.deleteById(showtime.getId());
        })).isInstanceOf(ConflictException.class);

        assertThat(showtimes.findById(showtime.getId())).isPresent();
        assertThat(reservations.findById(confirmed.getId())).isPresent();
        assertThat(reservations.findById(cancelled.getId())).as("cleanup rolled back").isPresent();
    }

    @Test
    void movieDeleteThatSlipsPastTheCheckKeepsConfirmedReservations() {
        Reservation confirmed = book("B2");

        assertThatThrownBy(() -> movies.deleteById(movie.getId())).isInstanceOf(ConflictException.class);

        assertThat(movies.findById(movie.getId())).isPresent();
        assertThat(reservations.findById(confirmed.getId())).isPresent();
    }

    @Test
    void movieWithOnlyCancelledReservationsCanBeDeleted() {
        Reservation cancelled = reservations.save(book("C3").cancel(showtime, LocalDateTime.now()));

        movieController.delete(movie.getId());

        assertThat(movies.findById(movie.getId())).isEmpty();
        assertThat(showtimes.findById(showtime.getId())).isEmpty();
        assertThat(reservations.findById(cancelled.getId())).isEmpty();
    }
}
