package reservation.movie.usecases.ports.input.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(LocalDate from, LocalDate to, BigDecimal totalRevenue, long totalReservations,
                                    long totalSeatsSold, List<MovieRevenueLine> movies) {

    public record MovieRevenueLine(Long movieId, String movieTitle, long reservations, long seatsSold,
                                   BigDecimal revenue) {
    }
}
