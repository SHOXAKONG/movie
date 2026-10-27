package reservation.movie.usecases.ports.input.showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScheduleShowtimeRequest(Long movieId, Long auditoriumId, LocalDateTime startTime, BigDecimal price) {
}
