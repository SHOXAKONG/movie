package reservation.movie.usecases.ports.input.showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeResponse(Long id, Long movieId, String movieTitle, Long auditoriumId, String auditoriumName,
                               LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, int capacity,
                               int availableSeats) {
}
