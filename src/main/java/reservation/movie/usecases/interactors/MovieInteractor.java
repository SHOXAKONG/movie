package reservation.movie.usecases.interactors;

import reservation.movie.entities.Movie;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.ports.input.movie.IMovieInputPort;
import reservation.movie.usecases.ports.input.movie.MovieRequest;
import reservation.movie.usecases.ports.input.movie.MovieResponse;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;
import reservation.movie.usecases.ports.output.transaction.ITransactionRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class MovieInteractor implements IMovieInputPort {

    private final IMovieRepository movies;
    private final IShowtimeRepository showtimes;
    private final IReservationRepository reservations;
    private final ITransactionRunner transactions;
    private final Clock clock;

    public MovieInteractor(IMovieRepository movies, IShowtimeRepository showtimes,
                           IReservationRepository reservations, ITransactionRunner transactions, Clock clock) {
        this.movies = movies;
        this.showtimes = showtimes;
        this.reservations = reservations;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public MovieResponse create(MovieRequest request) {
        Movie movie = Movie.create(request.title(), request.description(), request.posterUrl(),
                Responses.orZero(request.durationMinutes()), request.genres());
        return Responses.movie(movies.save(movie));
    }

    @Override
    public MovieResponse update(Long movieId, MovieRequest request) {
        Movie current = find(movieId);
        Movie updated = current.update(request.title(), request.description(), request.posterUrl(),
                Responses.orZero(request.durationMinutes()), request.genres());
        // Scheduled showtimes block their auditorium for the old runtime; changing it would break that plan.
        if (updated.getDurationMinutes() != current.getDurationMinutes()
                && showtimes.existsUnfinishedForMovie(movieId, LocalDateTime.now(clock))) {
            throw new ConflictException(
                    "Movie duration cannot change while it has upcoming showtimes; delete or finish them first");
        }
        return Responses.movie(movies.save(updated));
    }

    @Override
    public void delete(Long movieId) {
        transactions.run(() -> {
            find(movieId);
            if (reservations.existsActiveForMovie(movieId)) {
                throw new ConflictException("Movie has confirmed reservations and cannot be deleted");
            }
            reservations.deleteCancelledForMovie(movieId);
            // The database still refuses if a booking slipped in after the check above.
            movies.deleteById(movieId);
        });
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
