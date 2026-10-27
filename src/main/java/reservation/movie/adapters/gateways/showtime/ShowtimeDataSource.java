package reservation.movie.adapters.gateways.showtime;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShowtimeDataSource {

    /** Must throw {@code ConflictException} when the database rejects an overlapping showtime. */
    ShowtimeRecord save(ShowtimeRecord showtime);

    Optional<ShowtimeRecord> findById(Long id);

    List<ShowtimeRecord> findAllById(Collection<Long> ids);

    List<ShowtimeRecord> findStartingBetween(LocalDateTime from, LocalDateTime to);

    List<ShowtimeRecord> findOverlapping(Long auditoriumId, LocalDateTime start, LocalDateTime end);

    boolean existsEndingAfter(Long movieId, LocalDateTime time);

    /** Must throw {@code ConflictException} when reservations still reference the showtime. */
    void deleteById(Long id);
}
