package reservation.movie.adapters.presenters.movie;

import reservation.movie.usecases.ports.input.movie.MovieResponse;

import java.util.List;

public class MoviePresenter {

    public MovieViewModel present(MovieResponse movie) {
        return new MovieViewModel(movie.id(), movie.title(), movie.description() == null ? "" : movie.description(),
                movie.posterUrl(), movie.durationMinutes(), formatDuration(movie.durationMinutes()), movie.genres());
    }

    public List<MovieViewModel> present(List<MovieResponse> movies) {
        return movies.stream().map(this::present).toList();
    }

    static String formatDuration(int minutes) {
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}
