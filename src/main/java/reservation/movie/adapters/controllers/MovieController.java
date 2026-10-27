package reservation.movie.adapters.controllers;

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

    public MovieViewModel create(MovieRequest request) {
        return presenter.present(movies.create(request));
    }

    public MovieViewModel update(Long movieId, MovieRequest request) {
        return presenter.present(movies.update(movieId, request));
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
}
