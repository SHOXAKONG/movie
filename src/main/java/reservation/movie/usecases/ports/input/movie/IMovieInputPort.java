package reservation.movie.usecases.ports.input.movie;

import java.util.List;

public interface IMovieInputPort {

    MovieResponse create(MovieRequest request);

    MovieResponse update(Long movieId, MovieRequest request);

    void delete(Long movieId);

    MovieResponse get(Long movieId);

    /** All movies, or only those in {@code genre} when it is not blank. */
    List<MovieResponse> list(String genre);
}
