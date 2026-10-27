package reservation.movie.adapters.controllers;

import reservation.movie.adapters.presenters.reservation.ReservationPresenter;
import reservation.movie.adapters.presenters.reservation.ReservationViewModel;
import reservation.movie.usecases.ports.input.report.IReportInputPort;
import reservation.movie.usecases.ports.input.report.OccupancyResponse;
import reservation.movie.usecases.ports.input.report.RevenueReportResponse;

import java.time.LocalDate;
import java.util.List;

public class ReportController {

    private final IReportInputPort reports;
    private final ReservationPresenter reservationPresenter;

    public ReportController(IReportInputPort reports, ReservationPresenter reservationPresenter) {
        this.reports = reports;
        this.reservationPresenter = reservationPresenter;
    }

    public List<ReservationViewModel> reservations(Long showtimeId, LocalDate from, LocalDate to) {
        return reservationPresenter.present(reports.reservations(showtimeId, from, to));
    }

    public List<OccupancyResponse> occupancy(LocalDate from, LocalDate to) {
        return reports.occupancy(from, to);
    }

    public RevenueReportResponse revenue(LocalDate from, LocalDate to) {
        return reports.revenue(from, to);
    }
}
