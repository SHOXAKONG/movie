package reservation.movie.adapters.controllers;

import reservation.movie.adapters.controllers.request.MovieHttpRequest;
import reservation.movie.adapters.presenters.movie.MoviePresenter;
import reservation.movie.adapters.presenters.movie.MovieViewModel;
import reservation.movie.usecases.ports.input.movie.IMovieInputPort;
import reservation.movie.usecases.ports.input.movie.MovieRequest;

import java.util.List;

public class MovieController {

    private final IMovieInputPort movies;
    private final MoviePresenter presenter;

    public MovieController(IMovieInputPort movies, MoviePresenter presenter) {
        this.movies = movies;
        this.presenter = presenter;
    }

    public MovieViewModel create(MovieHttpRequest body) {
        return presenter.present(movies.create(toRequest(body)));
    }

    public MovieViewModel update(Long movieId, MovieHttpRequest body) {
        return presenter.present(movies.update(movieId, toRequest(body)));
    }

    public void delete(Long movieId) {
        movies.delete(movieId);
    }

    public MovieViewModel get(Long movieId) {
        return presenter.present(movies.get(movieId));
    }

    public List<MovieViewModel> list(String genre) {
        return presenter.present(movies.list(genre));
    }

    private static MovieRequest toRequest(MovieHttpRequest body) {
        return new MovieRequest(body.title(), body.description(), body.posterUrl(), body.durationMinutes(),
                body.genres());
    }
}
