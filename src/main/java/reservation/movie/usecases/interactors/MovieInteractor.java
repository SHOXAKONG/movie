package reservation.movie.usecases.interactors;

import reservation.movie.entities.Movie;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.ports.input.movie.IMovieInputPort;
import reservation.movie.usecases.ports.input.movie.MovieRequest;
import reservation.movie.usecases.ports.input.movie.MovieResponse;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;

import java.util.List;

public class MovieInteractor implements IMovieInputPort {

    private final IMovieRepository movies;
    private final IReservationRepository reservations;

    public MovieInteractor(IMovieRepository movies, IReservationRepository reservations) {
        this.movies = movies;
        this.reservations = reservations;
    }

    @Override
    public MovieResponse create(MovieRequest request) {
        Movie movie = Movie.create(request.title(), request.description(), request.posterUrl(),
                Responses.orZero(request.durationMinutes()), request.genres());
        return Responses.movie(movies.save(movie));
    }

    @Override
    public MovieResponse update(Long movieId, MovieRequest request) {
        Movie movie = find(movieId).update(request.title(), request.description(), request.posterUrl(),
                Responses.orZero(request.durationMinutes()), request.genres());
        return Responses.movie(movies.save(movie));
    }

    @Override
    public void delete(Long movieId) {
        find(movieId);
        if (reservations.existsActiveForMovie(movieId)) {
            throw new ConflictException("Movie has confirmed reservations and cannot be deleted");
        }
        movies.deleteById(movieId);
    }

    @Override
    public MovieResponse get(Long movieId) {
        return Responses.movie(find(movieId));
    }

    @Override
    public List<MovieResponse> list(String genre) {
        List<Movie> result = genre == null || genre.isBlank() ? movies.findAll() : movies.findByGenre(genre.strip());
        return result.stream().map(Responses::movie).toList();
    }

    private Movie find(Long movieId) {
        return movies.findById(movieId).orElseThrow(() -> NotFoundException.of("Movie", movieId));
    }
}
