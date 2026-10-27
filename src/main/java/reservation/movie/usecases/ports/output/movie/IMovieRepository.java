package reservation.movie.usecases.ports.output.movie;

import reservation.movie.entities.Movie;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IMovieRepository {

    Movie save(Movie movie);

    Optional<Movie> findById(Long id);

    List<Movie> findAll();

    List<Movie> findByGenre(String genre);

    List<Movie> findAllById(Collection<Long> ids);

    void deleteById(Long id);
}
