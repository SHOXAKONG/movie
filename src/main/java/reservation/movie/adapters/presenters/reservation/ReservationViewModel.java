package reservation.movie.adapters.presenters.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationViewModel(Long id, Long userId, Long showtimeId, String movieTitle, String auditorium,
                                   LocalDateTime startTime, List<String> seats, int seatCount, BigDecimal totalPrice,
                                   String status, LocalDateTime createdAt, LocalDateTime cancelledAt,
                                   boolean cancellable) {
}
