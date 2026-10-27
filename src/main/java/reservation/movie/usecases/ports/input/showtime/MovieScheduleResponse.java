package reservation.movie.usecases.ports.input.showtime;

import reservation.movie.usecases.ports.input.movie.MovieResponse;

import java.util.List;

public record MovieScheduleResponse(MovieResponse movie, List<ShowtimeResponse> showtimes) {
}
