package reservation.movie.adapters.controllers;

import reservation.movie.adapters.presenters.showtime.MovieScheduleViewModel;
import reservation.movie.adapters.presenters.showtime.SeatMapViewModel;
import reservation.movie.adapters.presenters.showtime.ShowtimePresenter;
import reservation.movie.adapters.presenters.showtime.ShowtimeViewModel;
import reservation.movie.usecases.ports.input.showtime.IShowtimeInputPort;
import reservation.movie.usecases.ports.input.showtime.ScheduleShowtimeRequest;

import java.time.LocalDate;
import java.util.List;

public class ShowtimeController {

    private final IShowtimeInputPort showtimes;
    private final ShowtimePresenter presenter;

    public ShowtimeController(IShowtimeInputPort showtimes, ShowtimePresenter presenter) {
        this.showtimes = showtimes;
        this.presenter = presenter;
    }

    public ShowtimeViewModel schedule(ScheduleShowtimeRequest request) {
        return presenter.present(showtimes.schedule(request));
    }

    public void delete(Long showtimeId) {
        showtimes.delete(showtimeId);
    }

    public ShowtimeViewModel get(Long showtimeId) {
        return presenter.present(showtimes.get(showtimeId));
    }

    public List<MovieScheduleViewModel> listForDate(LocalDate date) {
        return presenter.presentSchedule(showtimes.listForDate(date));
    }

    public SeatMapViewModel seatMap(Long showtimeId) {
        return presenter.presentSeatMap(showtimes.seatMap(showtimeId));
    }
}
