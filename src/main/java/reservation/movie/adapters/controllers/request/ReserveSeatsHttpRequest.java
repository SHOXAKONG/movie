package reservation.movie.adapters.controllers.request;

import java.util.List;

public record ReserveSeatsHttpRequest(Long showtimeId, List<String> seats) {
}
