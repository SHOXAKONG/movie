package reservation.movie.usecases.ports.output.showtime;

import reservation.movie.entities.Showtime;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IShowtimeRepository {

    /**
     * Implementations must reject an overlapping showtime with a {@code ConflictException},
     * even when two schedules race.
     */
    Showtime save(Showtime showtime);

    Optional<Showtime> findById(Long id);

    List<Showtime> findAllById(Collection<Long> ids);

    /** Showtimes starting in {@code [from, to)}, ordered by start time. */
    List<Showtime> findStartingBetween(LocalDateTime from, LocalDateTime to);

    List<Showtime> findOverlapping(Long auditoriumId, LocalDateTime start, LocalDateTime end);

    /** True if the movie has a showtime that has not finished at {@code now}. */
    boolean existsUnfinishedForMovie(Long movieId, LocalDateTime now);

    /** Must throw {@code ConflictException} if any reservation still references the showtime. */
    void deleteById(Long id);
}
