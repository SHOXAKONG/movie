package reservation.movie.adapters.gateways.showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeRecord(Long id, Long movieId, Long auditoriumId, LocalDateTime startTime,
                             LocalDateTime endTime, BigDecimal price) {
}
