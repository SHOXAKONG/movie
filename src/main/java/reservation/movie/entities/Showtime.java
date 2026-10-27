package reservation.movie.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public class Showtime {

    /** Time to clean the auditorium after a screening before the next one may start. */
    public static final Duration CLEANUP_TIME = Duration.ofMinutes(15);

    private final Long id;
    private final Long movieId;
    private final Long auditoriumId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final BigDecimal price;

    public Showtime(Long id, Long movieId, Long auditoriumId, LocalDateTime startTime, LocalDateTime endTime,
                    BigDecimal price) {
        if (movieId == null || auditoriumId == null) {
            throw new DomainException("A showtime needs a movie and an auditorium");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new DomainException("A showtime must end after it starts");
        }
        if (price == null || price.signum() < 0) {
            throw new DomainException("Ticket price must be zero or positive");
        }
        this.id = id;
        this.movieId = movieId;
        this.auditoriumId = auditoriumId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Schedules a new screening. The auditorium is blocked for the movie's runtime plus cleanup time.
     */
    public static Showtime schedule(Movie movie, Auditorium auditorium, LocalDateTime startTime, BigDecimal price,
                                    LocalDateTime now) {
        if (startTime == null || !startTime.isAfter(now)) {
            throw new DomainException("Showtimes can only be scheduled in the future");
        }
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes()).plus(CLEANUP_TIME);
        return new Showtime(null, movie.getId(), auditorium.getId(), startTime, endTime, price);
    }

    public boolean overlaps(Showtime other) {
        return auditoriumId.equals(other.auditoriumId)
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    public boolean hasStarted(LocalDateTime now) {
        return !now.isBefore(startTime);
    }

    public Long getId() {
        return id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Long getAuditoriumId() {
        return auditoriumId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
