package reservation.movie.unit;

import org.junit.jupiter.api.Test;
import reservation.movie.entities.Auditorium;
import reservation.movie.entities.DomainException;
import reservation.movie.entities.Movie;
import reservation.movie.entities.Reservation;
import reservation.movie.entities.ReservationStatus;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityRulesTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 27, 12, 0);

    private final Movie movie = Movie.create("Dune", "Sci-fi", null, 150, List.of("Sci-Fi", " sci-fi ", "Drama"))
            .withId(1L);
    private final Auditorium hall = new Auditorium(1L, "Hall 1", 5, 10);

    @Test
    void movieNormalizesGenresAndRequiresOne() {
        assertThat(movie.getGenres()).containsExactly("Sci-Fi", "Drama");
        assertThatThrownBy(() -> Movie.create("X", null, null, 90, List.of(" ")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void movieRejectsNonHttpPoster() {
        assertThatThrownBy(() -> Movie.create("X", null, "ftp://poster", 90, List.of("Drama")))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void seatParsesLabels() {
        assertThat(Seat.parse(" c12 ")).isEqualTo(new Seat('C', 12));
        assertThatThrownBy(() -> Seat.parse("12C")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Seat.parse("A0")).isInstanceOf(DomainException.class);
    }

    @Test
    void showtimeBlocksRuntimePlusCleanup() {
        Showtime showtime = Showtime.schedule(movie, hall, NOW.plusDays(1), new BigDecimal("9.5"), NOW);
        assertThat(showtime.getEndTime()).isEqualTo(NOW.plusDays(1).plusMinutes(150 + 15));
        assertThat(showtime.getPrice()).isEqualByComparingTo("9.50");
    }

    @Test
    void showtimeMustBeInTheFuture() {
        assertThatThrownBy(() -> Showtime.schedule(movie, hall, NOW.minusMinutes(1), BigDecimal.TEN, NOW))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void showtimesOverlapOnlyInSameAuditorium() {
        Showtime a = new Showtime(1L, 1L, 1L, NOW.plusHours(1), NOW.plusHours(3), BigDecimal.TEN);
        Showtime b = new Showtime(2L, 1L, 1L, NOW.plusHours(2), NOW.plusHours(4), BigDecimal.TEN);
        Showtime c = new Showtime(3L, 1L, 2L, NOW.plusHours(2), NOW.plusHours(4), BigDecimal.TEN);
        Showtime d = new Showtime(4L, 1L, 1L, NOW.plusHours(3), NOW.plusHours(5), BigDecimal.TEN);
        assertThat(a.overlaps(b)).isTrue();
        assertThat(a.overlaps(c)).isFalse();
        assertThat(a.overlaps(d)).isFalse();
    }

    @Test
    void reservationPricesSeatsAndValidatesThem() {
        Showtime showtime = new Showtime(7L, 1L, 1L, NOW.plusDays(1), NOW.plusDays(1).plusHours(3), new BigDecimal("12.00"));

        Reservation r = Reservation.create(9L, showtime, hall, List.of(Seat.parse("B2"), Seat.parse("A1")), NOW);

        assertThat(r.getTotalPrice()).isEqualByComparingTo("24.00");
        assertThat(r.getSeats()).extracting(Seat::label).containsExactly("A1", "B2");
        assertThatThrownBy(() -> Reservation.create(9L, showtime, hall, List.of(Seat.parse("F1")), NOW))
                .hasMessageContaining("does not exist");
        assertThatThrownBy(() -> Reservation.create(9L, showtime, hall, List.of(Seat.parse("A1"), Seat.parse("a1")), NOW))
                .hasMessageContaining("more than once");
    }

    @Test
    void onlyUpcomingConfirmedReservationsCanBeCancelled() {
        Showtime showtime = new Showtime(7L, 1L, 1L, NOW.plusHours(2), NOW.plusHours(5), BigDecimal.TEN);
        Reservation r = Reservation.create(9L, showtime, hall, List.of(Seat.parse("A1")), NOW);

        Reservation cancelled = r.cancel(showtime, NOW.plusHours(1));
        assertThat(cancelled.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThatThrownBy(() -> cancelled.cancel(showtime, NOW.plusHours(1))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> r.cancel(showtime, NOW.plusHours(3)))
                .hasMessageContaining("upcoming");
    }
}
