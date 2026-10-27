package reservation.movie.adapters.gateways.reservation;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationDataSource {

    /**
     * Inserts a new reservation or updates status/seat activity of an existing one.
     * Must throw {@code SeatsAlreadyReservedException} when the database rejects a double booking.
     */
    ReservationRecord save(ReservationRecord reservation);

    Optional<ReservationRecord> findById(Long id);

    List<ReservationRecord> findByUserId(Long userId);

    List<ReservationRecord> findByShowtimeId(Long showtimeId);

    List<ReservationRecord> findByShowtimeStartBetween(LocalDateTime from, LocalDateTime to);

    List<SeatRecord> findActiveSeats(Long showtimeId);

    Map<Long, Integer> countActiveSeats(Collection<Long> showtimeIds);

    boolean existsActiveForShowtime(Long showtimeId);

    boolean existsActiveForMovie(Long movieId);

    void deleteCancelledForShowtime(Long showtimeId);

    void deleteCancelledForMovie(Long movieId);
}
