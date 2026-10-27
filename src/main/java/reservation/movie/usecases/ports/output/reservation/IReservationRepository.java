package reservation.movie.usecases.ports.output.reservation;

import reservation.movie.entities.Reservation;
import reservation.movie.entities.Seat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IReservationRepository {

    /**
     * Inserts or updates a reservation. Implementations must throw
     * {@code SeatsAlreadyReservedException} if another active reservation holds one of its seats.
     */
    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByShowtimeId(Long showtimeId);

    /** All reservations whose showtime starts in {@code [from, to)}. */
    List<Reservation> findByShowtimeStartBetween(LocalDateTime from, LocalDateTime to);

    Set<Seat> findReservedSeats(Long showtimeId);

    /** showtimeId -> number of seats held by active reservations. */
    Map<Long, Integer> countReservedSeats(Collection<Long> showtimeIds);

    boolean existsActiveForShowtime(Long showtimeId);

    boolean existsActiveForMovie(Long movieId);
}
