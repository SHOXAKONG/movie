package reservation.movie.adapters.presenters.showtime;

import reservation.movie.adapters.presenters.movie.MovieViewModel;

import java.util.List;

public record MovieScheduleViewModel(MovieViewModel movie, List<ShowtimeViewModel> showtimes) {
}
