package reservation.movie.frameworks.db.datasource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.report.ReportDataSource;
import reservation.movie.frameworks.db.repository.ReportQueryRepository;
import reservation.movie.usecases.ports.output.report.MovieRevenue;
import reservation.movie.usecases.ports.output.report.ShowtimeOccupancy;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class JpaReportDataSource implements ReportDataSource {

    private final ReportQueryRepository queries;

    public JpaReportDataSource(ReportQueryRepository queries) {
        this.queries = queries;
    }

    @Override
    public List<ShowtimeOccupancy> occupancy(LocalDateTime from, LocalDateTime to) {
        return queries.occupancy(from, to).stream()
                .map(r -> new ShowtimeOccupancy(r.getShowtimeId(), r.getMovieTitle(), r.getAuditoriumName(),
                        r.getStartTime(), r.getCapacity(), r.getReservedSeats().intValue()))
                .toList();
    }

    @Override
    public List<MovieRevenue> revenueByMovie(LocalDateTime from, LocalDateTime to) {
        return queries.revenueByMovie(from, to).stream()
                .map(r -> new MovieRevenue(r.getMovieId(), r.getMovieTitle(), r.getReservations(), r.getSeatsSold(),
                        r.getRevenue()))
                .toList();
    }
}
