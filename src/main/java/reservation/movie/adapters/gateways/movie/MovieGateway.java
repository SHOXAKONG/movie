package reservation.movie.adapters.gateways.movie;

import reservation.movie.entities.Movie;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MovieGateway implements IMovieRepository {

    private final MovieDataSource dataSource;

    public MovieGateway(MovieDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Movie save(Movie movie) {
        return toEntity(dataSource.save(toRecord(movie)));
    }

    @Override
    public Optional<Movie> findById(Long id) {
        return dataSource.findById(id).map(MovieGateway::toEntity);
    }

    @Override
    public List<Movie> findAll() {
        return dataSource.findAll().stream().map(MovieGateway::toEntity).toList();
    }

    @Override
    public List<Movie> findByGenre(String genre) {
        return dataSource.findByGenre(genre).stream().map(MovieGateway::toEntity).toList();
    }

    @Override
    public List<Movie> findAllById(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : dataSource.findAllById(ids).stream().map(MovieGateway::toEntity).toList();
    }

    @Override
    public void deleteById(Long id) {
        dataSource.deleteById(id);
    }

    private static MovieRecord toRecord(Movie movie) {
        return new MovieRecord(movie.getId(), movie.getTitle(), movie.getDescription(), movie.getPosterUrl(),
                movie.getDurationMinutes(), movie.getGenres());
    }

    private static Movie toEntity(MovieRecord record) {
        return new Movie(record.id(), record.title(), record.description(), record.posterUrl(),
                record.durationMinutes(), record.genres());
    }
}
