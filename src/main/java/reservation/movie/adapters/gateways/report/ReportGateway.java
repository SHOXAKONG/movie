package reservation.movie.adapters.gateways.report;

import reservation.movie.usecases.ports.output.report.IReportRepository;
import reservation.movie.usecases.ports.output.report.MovieRevenue;
import reservation.movie.usecases.ports.output.report.ShowtimeOccupancy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Report rows are already use-case-shaped read models, so this gateway only delegates.
 */
public class ReportGateway implements IReportRepository {

    private final ReportDataSource dataSource;

    public ReportGateway(ReportDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ShowtimeOccupancy> occupancy(LocalDateTime from, LocalDateTime to) {
        return dataSource.occupancy(from, to);
    }

    @Override
    public List<MovieRevenue> revenueByMovie(LocalDateTime from, LocalDateTime to) {
        return dataSource.revenueByMovie(from, to);
    }
}
