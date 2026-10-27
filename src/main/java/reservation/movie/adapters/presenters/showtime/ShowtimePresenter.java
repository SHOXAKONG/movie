package reservation.movie.adapters.presenters.showtime;

import reservation.movie.adapters.presenters.movie.MoviePresenter;
import reservation.movie.usecases.ports.input.showtime.MovieScheduleResponse;
import reservation.movie.usecases.ports.input.showtime.SeatMapResponse;
import reservation.movie.usecases.ports.input.showtime.ShowtimeResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShowtimePresenter {

    private final MoviePresenter moviePresenter;

    public ShowtimePresenter(MoviePresenter moviePresenter) {
        this.moviePresenter = moviePresenter;
    }

    public ShowtimeViewModel present(ShowtimeResponse s) {
        return new ShowtimeViewModel(s.id(), s.movieId(), s.movieTitle(), s.auditoriumId(), s.auditoriumName(),
                s.startTime(), s.endTime(), s.price(), s.capacity(), s.availableSeats(), s.availableSeats() == 0);
    }

    public List<MovieScheduleViewModel> presentSchedule(List<MovieScheduleResponse> schedule) {
        return schedule.stream()
                .map(m -> new MovieScheduleViewModel(moviePresenter.present(m.movie()),
                        m.showtimes().stream().map(this::present).toList()))
                .toList();
    }

    public SeatMapViewModel presentSeatMap(SeatMapResponse map) {
        Map<String, List<SeatMapViewModel.SeatView>> rows = new LinkedHashMap<>();
        for (SeatMapResponse.SeatStatus seat : map.seats()) {
            rows.computeIfAbsent(seat.row(), r -> new ArrayList<>())
                    .add(new SeatMapViewModel.SeatView(seat.label(), seat.number(), seat.available()));
        }
        return new SeatMapViewModel(map.showtimeId(), map.rows() * map.seatsPerRow(), map.availableSeats(),
                rows.entrySet().stream().map(e -> new SeatMapViewModel.SeatRow(e.getKey(), e.getValue())).toList());
    }
}
