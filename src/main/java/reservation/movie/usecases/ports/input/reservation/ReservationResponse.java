package reservation.movie.usecases.ports.input.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(Long id, Long userId, Long showtimeId, String movieTitle, String auditoriumName,
                                  LocalDateTime startTime, List<String> seats, BigDecimal totalPrice, String status,
                                  LocalDateTime createdAt, LocalDateTime cancelledAt, boolean cancellable) {
}
