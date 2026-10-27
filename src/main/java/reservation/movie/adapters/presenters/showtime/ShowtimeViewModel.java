package reservation.movie.adapters.presenters.showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeViewModel(Long id, Long movieId, String movieTitle, Long auditoriumId, String auditorium,
                                LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, int capacity,
                                int availableSeats, boolean soldOut) {
}
