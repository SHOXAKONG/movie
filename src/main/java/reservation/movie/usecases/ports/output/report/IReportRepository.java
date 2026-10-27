package reservation.movie.usecases.ports.output.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only aggregate queries that would be too slow to compute entity by entity.
 */
public interface IReportRepository {

    List<ShowtimeOccupancy> occupancy(LocalDateTime from, LocalDateTime to);

    /** Revenue from confirmed reservations, grouped by movie, highest first. */
    List<MovieRevenue> revenueByMovie(LocalDateTime from, LocalDateTime to);
}
