package reservation.movie.usecases.interactors;

import reservation.movie.entities.Reservation;
import reservation.movie.usecases.ports.input.report.IReportInputPort;
import reservation.movie.usecases.ports.input.report.OccupancyResponse;
import reservation.movie.usecases.ports.input.report.RevenueReportResponse;
import reservation.movie.usecases.ports.input.reservation.ReservationResponse;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.report.IReportRepository;
import reservation.movie.usecases.ports.output.report.MovieRevenue;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class ReportInteractor implements IReportInputPort {

    private static final int DEFAULT_WINDOW_DAYS = 30;

    private final IReservationRepository reservations;
    private final IReportRepository reports;
    private final ReservationAssembler assembler;
    private final Clock clock;

    public ReportInteractor(IReservationRepository reservations, IReportRepository reports,
                            IShowtimeRepository showtimes, IMovieRepository movies, IAuditoriumRepository auditoriums,
                            Clock clock) {
        this.reservations = reservations;
        this.reports = reports;
        this.assembler = new ReservationAssembler(showtimes, movies, auditoriums, clock);
        this.clock = clock;
    }

    @Override
    public List<ReservationResponse> reservations(Long showtimeId, LocalDate from, LocalDate to) {
        List<Reservation> found;
        if (showtimeId != null) {
            found = reservations.findByShowtimeId(showtimeId);
        } else {
            DateRange range = defaultRange(from, to);
            found = reservations.findByShowtimeStartBetween(range.start(), range.end());
        }
        return assembler.toResponses(found);
    }

    @Override
    public List<OccupancyResponse> occupancy(LocalDate from, LocalDate to) {
        DateRange range = defaultRange(from, to);
        return reports.occupancy(range.start(), range.end()).stream()
                .map(o -> new OccupancyResponse(o.showtimeId(), o.movieTitle(), o.auditoriumName(), o.startTime(),
                        o.capacity(), o.reservedSeats(), o.capacity() - o.reservedSeats(),
                        percent(o.reservedSeats(), o.capacity())))
                .toList();
    }

    @Override
    public RevenueReportResponse revenue(LocalDate from, LocalDate to) {
        DateRange range = defaultRange(from, to);
        List<MovieRevenue> lines = reports.revenueByMovie(range.start(), range.end());
        BigDecimal total = lines.stream().map(MovieRevenue::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalReservations = lines.stream().mapToLong(MovieRevenue::reservations).sum();
        long totalSeats = lines.stream().mapToLong(MovieRevenue::seatsSold).sum();
        return new RevenueReportResponse(range.from(), range.to(), total, totalReservations, totalSeats,
                lines.stream()
                        .map(l -> new RevenueReportResponse.MovieRevenueLine(
                                l.movieId(), l.movieTitle(), l.reservations(), l.seatsSold(), l.revenue()))
                        .toList());
    }

    /** Defaults to the 30 days before and after today. */
    private DateRange defaultRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(clock);
        return DateRange.of(from, to, today.minusDays(DEFAULT_WINDOW_DAYS), today.plusDays(DEFAULT_WINDOW_DAYS));
    }

    private static double percent(int part, int whole) {
        return whole == 0 ? 0 : Math.round(part * 1000.0 / whole) / 10.0;
    }
}
