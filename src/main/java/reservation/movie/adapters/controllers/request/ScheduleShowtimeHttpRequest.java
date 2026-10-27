package reservation.movie.adapters.controllers.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScheduleShowtimeHttpRequest(Long movieId, Long auditoriumId, LocalDateTime startTime,
                                          BigDecimal price) {
}
