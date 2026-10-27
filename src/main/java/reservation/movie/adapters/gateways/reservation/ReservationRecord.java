package reservation.movie.adapters.gateways.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationRecord(Long id, Long userId, Long showtimeId, List<SeatRecord> seats, BigDecimal totalPrice,
                                String status, LocalDateTime createdAt, LocalDateTime cancelledAt) {
}
