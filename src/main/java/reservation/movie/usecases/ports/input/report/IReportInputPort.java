package reservation.movie.usecases.ports.input.report;

import reservation.movie.usecases.ports.input.reservation.ReservationResponse;

import java.time.LocalDate;
import java.util.List;

public interface IReportInputPort {

    /** Reservations for one showtime when {@code showtimeId} is given, otherwise for showtimes in [from, to]. */
    List<ReservationResponse> reservations(Long showtimeId, LocalDate from, LocalDate to);

    List<OccupancyResponse> occupancy(LocalDate from, LocalDate to);

    RevenueReportResponse revenue(LocalDate from, LocalDate to);
}
