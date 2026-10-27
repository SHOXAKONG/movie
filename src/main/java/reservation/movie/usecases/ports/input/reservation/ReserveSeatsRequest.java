package reservation.movie.usecases.ports.input.reservation;

import java.util.List;

public record ReserveSeatsRequest(Long showtimeId, List<String> seats) {
}
