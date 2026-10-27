package reservation.movie.adapters.gateways.report;

import reservation.movie.usecases.ports.output.report.MovieRevenue;
import reservation.movie.usecases.ports.output.report.ShowtimeOccupancy;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportDataSource {

    List<ShowtimeOccupancy> occupancy(LocalDateTime from, LocalDateTime to);

    List<MovieRevenue> revenueByMovie(LocalDateTime from, LocalDateTime to);
}
