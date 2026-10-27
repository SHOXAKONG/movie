package reservation.movie.usecases.ports.output.report;

import java.math.BigDecimal;

public record MovieRevenue(Long movieId, String movieTitle, long reservations, long seatsSold, BigDecimal revenue) {
}
